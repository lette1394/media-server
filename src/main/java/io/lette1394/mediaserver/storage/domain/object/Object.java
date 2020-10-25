package io.lette1394.mediaserver.storage.domain.object;

import static io.lette1394.mediaserver.storage.domain.object.ObjectEvents.DownloadAborted.downloadAborted;
import static io.lette1394.mediaserver.storage.domain.object.ObjectEvents.DownloadingTriggered.downloadingTriggered;
import static io.lette1394.mediaserver.storage.domain.object.ObjectEvents.UploadAborted.uploadAborted;
import static io.lette1394.mediaserver.storage.domain.object.ObjectEvents.Uploaded.uploaded;
import static io.lette1394.mediaserver.storage.domain.object.ObjectEvents.UploadingTriggered.uploadingTriggered;

import io.lette1394.mediaserver.common.AggregateRoot;
import io.lette1394.mediaserver.storage.domain.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.ControllableBinarySupplier;
import io.lette1394.mediaserver.storage.domain.ListenableBinarySupplier;
import io.lette1394.mediaserver.storage.domain.ListenableBinarySupplier.Listener;
import io.vavr.control.Try;
import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = "identifier", callSuper = false)
public abstract class Object extends AggregateRoot {
  public final Identifier identifier;

  protected final BinaryRepository binaryRepository;
  protected final Policy policy;
  protected final Attributes attributes;

  private final Snapshot currentSnapshot;

  private final AtomicLong progressedLength = new AtomicLong(0);

  protected Object(
    Identifier identifier,
    Attributes attributes,
    BinaryRepository binaryRepository,
    Policy policy) {

    this.identifier = identifier;
    this.attributes = attributes;
    this.binaryRepository = binaryRepository;
    this.policy = policy;
    this.currentSnapshot = Snapshot.initial(this);
  }

  protected abstract long getSize();

  public Snapshot getSnapshot() {
    return currentSnapshot;
  }

  public CompletableFuture<Object> upload(BinarySupplier binarySupplier) {
    final long length = binarySupplier.getLength();

    return checkBeforeUpload().toCompletableFuture()
      .thenCompose(__ -> upload0(wrap(binarySupplier)))
      .thenCompose(__1 ->
        // TODO: 실제 올라간 사이즈로 체크해야 하지 않나?
        checkAfterUploaded(binarySupplier.getLength()).map(__2 -> this).toCompletableFuture());
  }

  public CompletableFuture<BinarySupplier> download() {
    return checkBeforeDownload().toCompletableFuture()
      .thenCompose(__ -> binaryRepository.findBinary(identifier));
  }

  // TODO: rename
  protected abstract CompletableFuture<Void> upload0(BinarySupplier binarySupplier);

  protected abstract State getObjectState();

  private Try<Void> checkBeforeUpload() {
    addEvent(uploadingTriggered(this, binaryRepository));
    return policy
      .test(currentSnapshot.update(LifeCycle.BEFORE_UPLOAD).update(0L))
      .onFailure(this::abortUpload);
  }

  private Try<Void> checkDuringUploading(long currentSize) {
    // TODO: event 발행 -> 성능 문제가 있을 거 같은데...
    return policy
      .test(currentSnapshot.update(LifeCycle.DURING_UPLOADING).update(currentSize))
      .onFailure(this::abortUpload);
  }

  private Try<Void> checkAfterUploaded(long totalSize) {
    addEvent(uploaded(this, binaryRepository));
    return policy
      .test(currentSnapshot.update(LifeCycle.AFTER_UPLOADED).update(totalSize))
      .onFailure(this::abortUpload);
  }

  private Try<Void> checkBeforeDownload() {
    addEvent(downloadingTriggered(this));
    return policy
      .test(currentSnapshot.update(LifeCycle.BEFORE_DOWNLOAD).update(0L))
      .onFailure(this::abortDownload);
  }

  // TODO: exception handler를 위해 event listener 가 또 필요할까...?
  //  domain event 만으로 충분히 가능할 거 같은데... 안되려나
  //  한 번 해 보자.
  private void abortUpload(Throwable throwable) {
    addEvent(uploadAborted(this, throwable));
  }

  private void abortDownload(Throwable throwable) {
    addEvent(downloadAborted(this, throwable));
  }

  private BinarySupplier wrap(BinarySupplier binarySupplier) {
    final BinarySupplier listenableBinarySupplier = new ListenableBinarySupplier(
      binarySupplier, new Listener() {
      //TODO: 클래스로 빼자
      private boolean aborted = false;

      @Override
      public void transferAborted(Throwable throwable) {
        if (aborted) {
          return;
        }
        abortUpload(throwable);
        aborted = true;
      }

      @Override
      public void duringTransferring(long currentLength, long totalLength) {
        progressedLength.set(currentLength);
      }
    });

    return new ControllableBinarySupplier(
      listenableBinarySupplier, new ControllableBinarySupplier.Policy() {
      @Override
      public Try<Void> duringTransferring(long currentSize, long total) {
        return checkDuringUploading(currentSize);
      }
    });
  }

  public Tags getTags() {
    return attributes.getTags();
  }

  public OffsetDateTime getCreated() {
    return attributes.getCreated();
  }

  public OffsetDateTime getUpdated() {
    return attributes.getUpdated();
  }
}