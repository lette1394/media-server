package io.lette1394.mediaserver.storage.domain;

import static io.lette1394.mediaserver.storage.domain.BinaryLifecycle.AFTER_TRANSFERRED;
import static io.lette1394.mediaserver.storage.domain.BinaryLifecycle.BEFORE_TRANSFER;
import static io.lette1394.mediaserver.storage.domain.BinaryLifecycle.DURING_TRANSFERRING;
import static io.lette1394.mediaserver.storage.domain.BinaryLifecycle.TRANSFER_ABORTED;
import static io.lette1394.mediaserver.storage.domain.Command.DOWNLOAD;
import static io.lette1394.mediaserver.storage.domain.Command.UPLOAD;

import io.lette1394.mediaserver.common.AggregateRoot;
import io.lette1394.mediaserver.common.TimeStamp;
import io.lette1394.mediaserver.storage.domain.Events.DownloadRejected;
import io.lette1394.mediaserver.storage.domain.Events.DownloadingTriggered;
import io.lette1394.mediaserver.storage.domain.Events.UploadRejected;
import io.lette1394.mediaserver.storage.domain.Events.Uploaded;
import io.lette1394.mediaserver.storage.domain.Events.UploadingTriggered;
import io.vavr.control.Try;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Delegate;
import org.reactivestreams.Publisher;

@EqualsAndHashCode(of = "identifier", callSuper = false)
public class Object<BUFFER extends Payload> extends AggregateRoot {

  @Getter
  private final Identifier identifier;
  private final ObjectPolicy objectPolicy;
  private final ObjectSnapshot objectSnapshot;
  private final Tags tags;
  @Delegate
  private final TimeStamp timeStamp;
  private final BinaryPolicy binaryPolicy;
  private final BinarySnapshot binarySnapshot;
  private final BinaryRepository<BUFFER> binaryRepository;

  @Builder
  public Object(Identifier identifier,
    ObjectPolicy objectPolicy,
    ObjectSnapshot objectSnapshot,
    Tags tags,
    TimeStamp timeStamp,
    BinaryPolicy binaryPolicy,
    BinarySnapshot binarySnapshot,
    BinaryRepository<BUFFER> binaryRepository) {
    this.identifier = identifier;
    this.objectPolicy = objectPolicy;
    this.objectSnapshot = objectSnapshot;
    this.tags = tags;
    this.timeStamp = timeStamp;
    this.binaryPolicy = binaryPolicy;
    this.binarySnapshot = binarySnapshot;
    this.binaryRepository = binaryRepository;
  }

  public BinarySupplier<BUFFER> upload(Publisher<BUFFER> upstream) {
    return objectPolicy.test(objectSnapshot.update(UPLOAD))
      .onSuccess(__ -> addEvent(UploadingTriggered.uploadingTriggered()))
      .onFailure(e -> addEvent(UploadRejected.uploadRejected(e)))
      .map(__ -> toSupplier(upstream))
      .getOrElseThrow(e -> new OperationCanceled(UPLOAD, e));
  }

  public CompletableFuture<BinarySupplier<BUFFER>> download() {
    return objectPolicy.test(objectSnapshot.update(DOWNLOAD))
      .onSuccess(__ -> addEvent(DownloadingTriggered.downloadingTriggered()))
      .onFailure(e -> addEvent(DownloadRejected.downloadRejected(e)))
      .toCompletableFuture()
      .thenCompose(__ -> binaryRepository.find(binaryPath()))
      .exceptionally(e -> {
        throw new OperationCanceled(DOWNLOAD, e);
      });
  }

  public long getSize() {
    return objectSnapshot.getSize();
  }

  public ObjectType getType() {
    return objectSnapshot.getObjectType();
  }

  public Tags getTags() {
    return tags;
  }

  BinarySupplier<BUFFER> toSupplier(Publisher<BUFFER> publisher) {
    return composeControllable(composeListenable(() -> publisher));
  }

  private BinaryPath binaryPath() {
    return new BinaryPath() {
      @Override
      public String asString() {
        return String.format("%s/%s", identifier.getArea(), identifier.getKey());
      }
    };
  }

  private BinarySupplier<BUFFER> composeControllable(BinarySupplier<BUFFER> binarySupplier) {
    return new ControllableBinarySupplier<>(binarySupplier, policy());
  }

  private BinarySupplier<BUFFER> composeListenable(BinarySupplier<BUFFER> binarySupplier) {
    return new ListenableBinarySupplier<>(binarySupplier, listener());
  }

  private ControllableBinarySupplier.Policy policy() {
    return new ControllableBinarySupplier.Policy() {
      @Override
      public Try<Void> beforeTransfer() {
        return binaryPolicy.test(
          binarySnapshot.update(BEFORE_TRANSFER));
      }

      @Override
      public Try<Void> duringTransferring(long currentLength) {
        return binaryPolicy.test(
          binarySnapshot
            .update(currentLength)
            .update(DURING_TRANSFERRING));
      }

      @Override
      public Try<Void> afterTransferred(long totalLength) {
        return binaryPolicy.test(
          binarySnapshot
            .update(totalLength)
            .update(AFTER_TRANSFERRED));
      }
    };
  }

  private ListenableBinarySupplier.Listener listener() {
    return new ListenableBinarySupplier.Listener() {
      private boolean aborted = false;

      @Override
      public void beforeTransfer() {
        binarySnapshot.update(BEFORE_TRANSFER);
      }

      @Override
      public void duringTransferring(long currentLength) {
        binarySnapshot
          .update(currentLength)
          .update(DURING_TRANSFERRING);
        objectSnapshot
          .update(currentLength);
      }

      @Override
      public void afterTransferred(long totalLength) {
        binarySnapshot
          .update(totalLength)
          .update(AFTER_TRANSFERRED);
        objectSnapshot
          .update(totalLength)
          .update(ObjectType.FULFILLED);
        addEvent(Uploaded.uploaded());
      }

      @Override
      public void transferAborted(Throwable throwable) {
        if (aborted) {
          return;
        }
        aborted = true;
        binarySnapshot.update(TRANSFER_ABORTED);
        objectSnapshot.update(ObjectType.PENDING);
        addEvent(Events.UploadAborted.uploadAborted(throwable));
      }
    };
  }
}
