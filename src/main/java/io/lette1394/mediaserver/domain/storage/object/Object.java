package io.lette1394.mediaserver.domain.storage.object;

import static io.lette1394.mediaserver.domain.storage.object.Policies.allowIfPassed;

import io.lette1394.mediaserver.common.AggregateRoot;
import io.lette1394.mediaserver.domain.storage.object.ObjectEvents.DownloadingTriggered;
import io.lette1394.mediaserver.domain.storage.object.ObjectEvents.Uploaded;
import io.lette1394.mediaserver.domain.storage.object.ObjectEvents.UploadingTriggered;
import java.util.concurrent.CompletableFuture;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = "identifier", callSuper = false)
public abstract class Object extends AggregateRoot {
  public final Identifier identifier;

  protected final BinaryRepository binaryRepository;
  protected final ObjectLifecyclePolicy objectLifecyclePolicy;
  protected final Attributes attributes;

  protected Object(
    Identifier identifier,
    Attributes attributes,
    BinaryRepository binaryRepository,
    ObjectLifecyclePolicy objectLifecyclePolicy) {

    this.identifier = identifier;
    this.attributes = attributes;
    this.binaryRepository = binaryRepository;
    this.objectLifecyclePolicy = objectLifecyclePolicy;
  }

  public CompletableFuture<Void> upload(BinarySupplier binarySupplier) {
    return beforeUploading()
      .thenCompose(__ -> upload0(binarySupplier))
      .thenCompose(__ -> afterUploaded());
  }

  // TODO: rename
  protected abstract CompletableFuture<Void> upload0(BinarySupplier binarySupplier);

  public CompletableFuture<BinarySupplier> download() {
    return beforeDownloading()
      .thenCompose(__ -> binaryRepository.findBinary(this));
  }

  public abstract boolean isInitial();

  public abstract boolean isPending();

  public abstract boolean isFulfilled();

  public abstract long getSize();

  private CompletableFuture<Void> beforeUploading() {
    addEvent(UploadingTriggered.UploadingTriggered(this, binaryRepository));
    return objectLifecyclePolicy
      .beforeUploading(this, binaryRepository)
      .thenAccept(allowIfPassed());
  }

  private CompletableFuture<Void> afterUploaded() {
    addEvent(Uploaded.uploaded(this, binaryRepository));
    return objectLifecyclePolicy
      .afterUploaded(this, binaryRepository)
      .thenAccept(allowIfPassed());
  }

  private CompletableFuture<Void> beforeDownloading() {
    addEvent(DownloadingTriggered.downloadingTriggered(this));
    return objectLifecyclePolicy
      .beforeDownloading(this)
      .thenAccept(allowIfPassed());
  }
}
