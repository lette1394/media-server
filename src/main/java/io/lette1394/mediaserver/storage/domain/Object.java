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
import io.lette1394.mediaserver.storage.domain.Events.UploadingTriggered;
import io.vavr.control.Try;
import java.util.concurrent.CompletableFuture;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Delegate;
import org.reactivestreams.Publisher;

@EqualsAndHashCode(of = "identifier", callSuper = false)
public abstract class Object<BUFFER extends SizeAware> extends AggregateRoot {

  protected Object(Identifier identifier,
    ObjectPolicy objectPolicy, BinaryPolicy binaryPolicy,
    Tags tags, TimeStamp timeStamp,
    BinarySnapshot binarySnapshot,
    BinaryRepository<BUFFER> binaryRepository) {
    this.identifier = identifier;
    this.objectPolicy = objectPolicy;
    this.binaryPolicy = binaryPolicy;
    this.tags = tags;
    this.timeStamp = timeStamp;
    this.binarySnapshot = binarySnapshot;
    this.binaryRepository = binaryRepository;
  }

  @Getter
  protected final Identifier identifier;
  protected final ObjectPolicy objectPolicy;

  protected final Tags tags;
  @Delegate
  protected final TimeStamp timeStamp;


  protected final BinaryPolicy binaryPolicy;
  protected final BinarySnapshot binarySnapshot;
  protected final BinaryRepository<BUFFER> binaryRepository;

  public BinarySupplier<BUFFER> upload(Publisher<BUFFER> upstream) {
    return objectPolicy.test(snapshot(UPLOAD))
      .onSuccess(__ -> addEvent(UploadingTriggered.uploadingTriggered()))
      .onFailure(e -> addEvent(UploadRejected.uploadRejected(e)))
      .map(__ -> toSupplier(upstream))
      .getOrElseThrow(() -> new OperationCanceled(UPLOAD));
  }

  protected abstract CompletableFuture<Void> doUpload(BinarySupplier<BUFFER> binarySupplier);

  public Publisher<BUFFER> download() {
    return objectPolicy.test(snapshot(DOWNLOAD))
      .onSuccess(__ -> addEvent(DownloadingTriggered.downloadingTriggered()))
      .onFailure(e -> addEvent(DownloadRejected.downloadRejected(e)))
      .map(__ -> doDownload())
      .getOrElseThrow(() -> new OperationCanceled(DOWNLOAD));
  }

  protected abstract Publisher<BUFFER> doDownload();

  private ObjectSnapshot snapshot(Command command) {
    return ObjectSnapshot.builder()
      .identifier(identifier)
      .command(command)
      .size(getSize())
      .objectType(getType())
      .build();
  }

  public abstract long getSize();

  public abstract ObjectType getType();

  public Tags getTags() {
    return tags;
  }

  Publisher<BUFFER> toPublisher() {
    return null;
  }

  BinarySupplier<BUFFER> toSupplier(Publisher<BUFFER> publisher) {
    return composeControllable(composeListenable(() -> publisher));
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
        return binaryPolicy.test(binarySnapshot.update(BEFORE_TRANSFER));
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
      }

      @Override
      public void afterTransferred(long totalLength) {
        binarySnapshot
          .update(totalLength)
          .update(AFTER_TRANSFERRED);
      }

      @Override
      public void transferAborted(Throwable throwable) {
        if (aborted) {
          return;
        }
        aborted = true;
        binarySnapshot.update(TRANSFER_ABORTED);
      }
    };
  }
}
