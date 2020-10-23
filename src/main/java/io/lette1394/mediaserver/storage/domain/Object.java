package io.lette1394.mediaserver.storage.domain;

import static io.lette1394.mediaserver.storage.domain.Policies.runIfPassed;
import static io.lette1394.mediaserver.storage.domain.Policies.runNextIfPassed;

import io.lette1394.mediaserver.common.AggregateRoot;
import io.lette1394.mediaserver.common.Result;
import io.lette1394.mediaserver.storage.domain.ControllableBinarySupplier.Policy;
import io.lette1394.mediaserver.storage.domain.ObjectEvents.DownloadingTriggered;
import io.lette1394.mediaserver.storage.domain.ObjectEvents.Uploaded;
import io.lette1394.mediaserver.storage.domain.ObjectEvents.UploadingTriggered;
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

  public abstract long getProgressingSize();

  public CompletableFuture<Result<Void>> upload(BinarySupplier binarySupplier) {
    return runIfPassed(beforeUpload())
      .thenCompose(__ -> upload0(wrap(binarySupplier))
        .thenApply(runNextIfPassed(this::afterUploaded)));
  }

  public CompletableFuture<Result<BinarySupplier>> download() {
    return runIfPassed(beforeDownload())
      .thenCompose(__ -> binaryRepository.findBinary(identifier));
  }

  // TODO: rename

  protected abstract CompletableFuture<Result<Void>> upload0(BinarySupplier binarySupplier);

  protected abstract ObjectState getObjectState();

  private Result<Void> beforeUpload() {
    addEvent(UploadingTriggered.UploadingTriggered(this, binaryRepository));

    return objectPolicy.test(snapshot(ObjectLifeCycle.BEFORE_UPLOAD));
  }

  private Result<Void> duringUploading() {
    // event 발행 -> 성능 문제가 있을 거 같은데...
    return objectPolicy.test(snapshot(ObjectLifeCycle.DURING_UPLOADING));
  }

  private Result<Void> afterUploaded() {
    addEvent(Uploaded.uploaded(this, binaryRepository));

    return objectPolicy.test(snapshot(ObjectLifeCycle.AFTER_UPLOADED));
  }

  private Result<Void> beforeDownload() {
    addEvent(DownloadingTriggered.downloadingTriggered(this));

    return objectPolicy.test(snapshot(ObjectLifeCycle.BEFORE_DOWNLOAD));
  }

  private ObjectSnapshot snapshot(ObjectLifeCycle lifeCycle) {
    return ObjectSnapshot.builder()
      .identifier(identifier)
      .lifeCycle(lifeCycle)
      .state(getObjectState())
      .size(getSize())
      .progressingSize(getProgressingSize())
      .build();
  }

  private BinarySupplier wrap(BinarySupplier binarySupplier) {
    return new ControllableBinarySupplier(
      binarySupplier, new Policy() {
      @Override
      public Result<Void> duringTransferring(long currentSize, long total) {
        return duringUploading();
      }
    });
  }
}
