package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.AggregateRoot;
import io.lette1394.mediaserver.storage.domain.ControllableBinarySupplier.Policy;
import io.lette1394.mediaserver.storage.domain.ListenableBinarySupplier.Listener;
import io.lette1394.mediaserver.storage.domain.ObjectEvents.DownloadingTriggered;
import io.lette1394.mediaserver.storage.domain.ObjectEvents.UploadAborted;
import io.lette1394.mediaserver.storage.domain.ObjectEvents.Uploaded;
import io.lette1394.mediaserver.storage.domain.ObjectEvents.UploadingTriggered;
import io.vavr.control.Try;
import java.util.concurrent.CompletableFuture;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = "identifier", callSuper = false)
public abstract class Object extends AggregateRoot {
  public final Identifier identifier;

  protected final BinaryRepository binaryRepository;
  protected final ObjectPolicy objectPolicy;
  protected final Attributes attributes;

  protected Object(
    Identifier identifier,
    Attributes attributes,
    BinaryRepository binaryRepository,
    ObjectPolicy objectPolicy) {

    this.identifier = identifier;
    this.attributes = attributes;
    this.binaryRepository = binaryRepository;
    this.objectPolicy = objectPolicy;
  }

  public abstract long getSize();

  public CompletableFuture<Object> upload(BinarySupplier binarySupplier) {
    final Try<Integer> success = Try.success(123);
    final CompletableFuture<Long> longCompletableFuture = CompletableFuture.completedFuture(55L);

//
//    int input = 2;
//    String output = Match(input).of(
//      Case($(1), "one"),
//      Case($(2), "two"),
//      Case($(3), "three"),
//      Case($(), "?"));
//
//    Integer a =  Match(success).of(
//      Case($(Try::isSuccess), Try::get),
//      Case($(), () -> null));

    return checkBeforeUpload().toCompletableFuture()
      .thenCompose(__ -> upload0(wrap(binarySupplier)))
      .thenCompose(__1 ->
        checkAfterUploaded(binarySupplier.getSize()).map(__2 -> this)
          .toCompletableFuture());
  }

  public CompletableFuture<BinarySupplier> download() {
    return checkBeforeDownload().toCompletableFuture()
      .thenCompose(__ -> binaryRepository.findBinary(identifier));
  }

  // TODO: rename
  protected abstract CompletableFuture<Void> upload0(BinarySupplier binarySupplier);

  protected abstract ObjectState getObjectState();

  private Try<Void> checkBeforeUpload() {
    addEvent(UploadingTriggered.UploadingTriggered(this, binaryRepository));

    return objectPolicy.test(snapshot(ObjectLifeCycle.BEFORE_UPLOAD, 0L));
  }

  private Try<Void> checkDuringUploading(long currentSize) {
    // event 발행 -> 성능 문제가 있을 거 같은데...
    return objectPolicy.test(snapshot(ObjectLifeCycle.DURING_UPLOADING, currentSize));
  }

  private Try<Void> checkAfterUploaded(long totalSize) {
    addEvent(Uploaded.uploaded(this, binaryRepository));

    return objectPolicy.test(snapshot(ObjectLifeCycle.AFTER_UPLOADED, totalSize));
  }

  private Try<Void> checkBeforeDownload() {
    addEvent(DownloadingTriggered.downloadingTriggered(this));

    return objectPolicy.test(snapshot(ObjectLifeCycle.BEFORE_DOWNLOAD, 0L));
  }

  // TODO: exception handler를 위해 event listener 가 또 필요할까...?
  //  domain event 만으로 충분히 가능할 거 같은데... 안되려나
  //  한 번 해 보자.
  private void uploadAborted(Throwable throwable) {
    addEvent(UploadAborted.uploadAborted(this, throwable));
  }

  private ObjectSnapshot snapshot(ObjectLifeCycle lifeCycle, long progressingSize) {
    return ObjectSnapshot.builder()
      .identifier(identifier)
      .lifeCycle(lifeCycle)
      .state(getObjectState())
      .size(getSize())
      .progressingSize(progressingSize)
      .build();
  }

  private BinarySupplier wrap(BinarySupplier binarySupplier) {
    final BinarySupplier listenableBinarySupplier = new ListenableBinarySupplier(
      binarySupplier, new Listener() {
      @Override
      public void transferAborted(Throwable throwable) {
        uploadAborted(throwable);
      }
    });

    return new ControllableBinarySupplier(
      listenableBinarySupplier, new Policy() {
      @Override
      public Try<Void> duringTransferring(long currentSize, long total) {
        return checkDuringUploading(currentSize);
      }
    });
  }
}
