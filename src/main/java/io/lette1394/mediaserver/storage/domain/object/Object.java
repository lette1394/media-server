package io.lette1394.mediaserver.storage.domain.object;

import static io.lette1394.mediaserver.storage.domain.object.Events.DownloadAborted.downloadAborted;
import static io.lette1394.mediaserver.storage.domain.object.Events.DownloadingTriggered.downloadingTriggered;
import static io.lette1394.mediaserver.storage.domain.object.Events.UploadAborted.uploadAborted;
import static io.lette1394.mediaserver.storage.domain.object.Events.Uploaded.uploaded;
import static io.lette1394.mediaserver.storage.domain.object.Events.UploadingTriggered.uploadingTriggered;

import io.lette1394.mediaserver.common.AggregateRoot;
import io.lette1394.mediaserver.common.TimeStamp;
import io.lette1394.mediaserver.storage.domain.binary.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.binary.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.binary.ControllableBinarySupplier;
import io.lette1394.mediaserver.storage.domain.binary.ListenableBinarySupplier;
import io.lette1394.mediaserver.storage.domain.binary.ListenableBinarySupplier.Listener;
import io.vavr.control.Try;
import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;
import lombok.EqualsAndHashCode;
import lombok.Getter;


// TODO:
//  Async 방식만 지원 -> Async는 Sync의 super set 이라는 판단
//  -->> async -> sync (can) / sync -> async (can't)
//  Async/Sync를 모두 지원할 수 없음 -> 이런 경우는 결국 usecase에서도 갈라지는데 이러면 Object가 이렇게 handling 하는 이유를 찾을 수 없다
//  1. Object<BUFFER_TYPE> 으로 변경
//  2. BinarySupplier는 async 모드만 지원, 아마도 단순한 Publisher supplier가 될 듯
//  3. (이거 필요한지 검토) 그러나 테스트의 용이성을 위해 기존 sync 방식은 지원
//    --> 이거그냥 AsyncAggregateReader 이랑 SingleThreadInputStreamPublisher 이거 쓰면 되는거 아니냐?
@EqualsAndHashCode(of = "identifier", callSuper = false)
public abstract class Object extends AggregateRoot {
  @Getter
  protected final Identifier identifier;

  protected final Tags tags;
  protected final Snapshot currentSnapshot;
  protected final TimeStamp timeStamp;

  protected final Policy policy;
  protected final BinaryRepository binaryRepository;

  protected Object(
    Identifier identifier,
    BinaryRepository binaryRepository,
    Policy policy, TimeStamp timeStamp, Tags tags) {

    this.identifier = identifier;
    this.binaryRepository = binaryRepository;
    this.policy = policy;
    this.timeStamp = timeStamp;
    this.tags = tags;
    this.currentSnapshot = Snapshot.initial(this);
  }

  protected abstract long getSize();

  public Snapshot getSnapshot() {
    return currentSnapshot;
  }

  public CompletableFuture<Object> upload(BinarySupplier binarySupplier) {
    return checkBeforeUpload().toCompletableFuture()
      .thenCompose(__ -> upload0(wrap(binarySupplier)))
      .thenCompose(__1 ->
        checkAfterUploaded(currentSnapshot.getProgressingSize())
          .map(__2 -> this).toCompletableFuture());
  }

  public CompletableFuture<? extends BinarySupplier> download() {
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
      public void duringTransferring(long currentLength) {
        currentSnapshot.update(currentLength);
      }

      @Override
      public void afterTransferred(long totalLength) {
        currentSnapshot.update(totalLength);
      }
    });

    return new ControllableBinarySupplier(
      listenableBinarySupplier, new ControllableBinarySupplier.Policy() {
      @Override
      public Try<Void> duringTransferring(long currentLength) {
        return checkDuringUploading(currentLength);
      }
    });
  }

  public Tags getTags() {
    return tags;
  }

  public OffsetDateTime getCreated() {
    return timeStamp.getCreated();
  }

  public OffsetDateTime getUpdated() {
    return timeStamp.getUpdated();
  }
}
