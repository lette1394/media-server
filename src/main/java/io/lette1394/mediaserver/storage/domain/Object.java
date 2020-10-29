package io.lette1394.mediaserver.storage.domain;

import static io.lette1394.mediaserver.storage.domain.BinaryLifecycle.AFTER_TRANSFERRED;
import static io.lette1394.mediaserver.storage.domain.BinaryLifecycle.BEFORE_TRANSFER;
import static io.lette1394.mediaserver.storage.domain.BinaryLifecycle.DURING_TRANSFERRING;
import static io.lette1394.mediaserver.storage.domain.BinaryLifecycle.TRANSFER_ABORTED;
import static io.lette1394.mediaserver.storage.domain.Command.UPLOAD;

import io.lette1394.mediaserver.common.AggregateRoot;
import io.lette1394.mediaserver.common.TimeStamp;
import io.lette1394.mediaserver.storage.domain.Events.UploadRejected;
import io.lette1394.mediaserver.storage.domain.Events.UploadingTriggered;
import io.vavr.control.Try;
import lombok.Builder;
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
    BinarySupplier<BUFFER> binarySupplier) {
    this.identifier = identifier;
    this.objectPolicy = objectPolicy;
    this.binaryPolicy = binaryPolicy;
    this.tags = tags;
    this.timeStamp = timeStamp;
    this.binarySnapshot = binarySnapshot;
    this.binarySupplier = binarySupplier;
  }

  @Getter
  protected final Identifier identifier;
  protected final ObjectPolicy objectPolicy;
  private final BinaryPolicy binaryPolicy;

  protected final Tags tags;
  @Delegate
  protected final TimeStamp timeStamp;

  private final BinarySnapshot binarySnapshot;
  private final BinarySupplier<BUFFER> binarySupplier;


  public BinarySupplier<BUFFER> upload(Publisher<BUFFER> upstream) {
    return objectPolicy.test(snapshot(UPLOAD))
      .onSuccess(__ -> uploadingTriggered())
      .onFailure(e -> uploadRejected(e))
      .map(__ -> toSupplier(upstream))
      .getOrElseThrow(() -> new OperationCanceled(UPLOAD));
  }

  private void uploadRejected(Throwable e) {
    addEvent(UploadRejected.uploadRejected(e));
  }

  private void uploadingTriggered() {
    addEvent(UploadingTriggered.uploadingTriggered());
  }

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
