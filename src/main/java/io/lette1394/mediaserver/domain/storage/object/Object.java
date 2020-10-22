package io.lette1394.mediaserver.domain.storage.object;

import static io.lette1394.mediaserver.domain.storage.object.Policies.runNextIfPassed;

import io.lette1394.mediaserver.common.AggregateRoot;
import io.lette1394.mediaserver.common.Result;
import io.lette1394.mediaserver.domain.storage.object.ObjectEvents.DownloadingTriggered;
import io.lette1394.mediaserver.domain.storage.object.ObjectEvents.Uploaded;
import io.lette1394.mediaserver.domain.storage.object.ObjectEvents.UploadingTriggered;
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

  public CompletableFuture<Result> upload(BinarySupplier binarySupplier) {
    return beforeUploading()
      .thenCompose(runNextIfPassed(upload0(binarySupplier)))
      .thenCompose(__ -> afterUploaded());
  }

  // TODO: rename
  protected abstract CompletableFuture<Result> upload0(BinarySupplier binarySupplier);

  public CompletableFuture<BinarySupplier> download() {
    return beforeDownloading()
      .thenCompose(__ -> binaryRepository.findBinary(identifier));
  }

  protected abstract ObjectState getObjectState();

  public abstract long getSize();

  private CompletableFuture<Result> beforeUploading() {
    addEvent(UploadingTriggered.UploadingTriggered(this, binaryRepository));

    return objectPolicy.test(snapshot(ObjectLifeCycle.BEFORE_UPLOADING));
  }

  private CompletableFuture<Result> afterUploaded() {
    addEvent(Uploaded.uploaded(this, binaryRepository));

    return objectPolicy.test(snapshot(ObjectLifeCycle.AFTER_UPLOADED));
  }

  private CompletableFuture<Result> beforeDownloading() {
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
