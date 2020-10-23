package io.lette1394.mediaserver.storage.domain;

import static io.lette1394.mediaserver.storage.domain.Policies.runNextIfPassed;

import io.lette1394.mediaserver.common.AggregateRoot;
import io.lette1394.mediaserver.common.Result;
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




  public CompletableFuture<Result<Void>> upload(BinarySupplier binarySupplier) {
    return beforeUploading().thenCompose(
      runNextIfPassed(upload0(binarySupplier)
        .thenCompose(__ -> afterUploaded())));
  }

  public CompletableFuture<Result<BinarySupplier>> download() {
    return beforeDownloading()
      .thenCompose(runNextIfPassed(binaryRepository.findBinary(identifier)));
  }

  // TODO: rename
  protected abstract CompletableFuture<Result<Void>> upload0(BinarySupplier binarySupplier);

  protected abstract ObjectState getObjectState();

  private CompletableFuture<Result<Void>> beforeUploading() {
    addEvent(UploadingTriggered.UploadingTriggered(this, binaryRepository));

    return objectPolicy.test(snapshot(ObjectLifeCycle.BEFORE_UPLOADING));
  }

  private CompletableFuture<Result<Void>> afterUploaded() {
    addEvent(Uploaded.uploaded(this, binaryRepository));

    return objectPolicy.test(snapshot(ObjectLifeCycle.AFTER_UPLOADED));
  }

  private CompletableFuture<Result<Void>> beforeDownloading() {
    addEvent(DownloadingTriggered.downloadingTriggered(this));

    return objectPolicy.test(snapshot(ObjectLifeCycle.BEFORE_DOWNLOADING));
  }

  private ObjectSnapshot snapshot(ObjectLifeCycle lifeCycle) {
    return ObjectSnapshot.builder()
      .identifier(identifier)
      .lifeCycle(lifeCycle)
      .state(getObjectState())
      .size(getSize())
      .build();
  }
}
