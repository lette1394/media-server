package io.lette1394.mediaserver.domain.storage.object;

import static io.lette1394.mediaserver.domain.storage.object.Policies.allowIfPassed;

import java.util.concurrent.CompletableFuture;
import lombok.EqualsAndHashCode;

// TODO: entity 표현
@EqualsAndHashCode(of = "identifier")
public abstract class Object {
  public final Identifier identifier;
  private final Attributes attributes;

  private final ObjectUploadPolicy objectUploadPolicy;
  private final ObjectDownloadPolicy objectDownloadPolicy;

  protected final BinaryRepository binaryRepository;

  protected Object(
    Identifier identifier,
    Attributes attributes,
    BinaryRepository binaryRepository,
    ObjectUploadPolicy objectUploadPolicy,
    ObjectDownloadPolicy objectDownloadPolicy) {

    this.identifier = identifier;
    this.attributes = attributes;
    this.binaryRepository = binaryRepository;
    this.objectUploadPolicy = objectUploadPolicy;
    this.objectDownloadPolicy = objectDownloadPolicy;
  }

  public CompletableFuture<Void> upload(BinarySupplier binarySupplier) {
    return objectUploadPolicy.test(this, binaryRepository)
      .thenAccept(allowIfPassed())
      .thenCompose(__ -> upload0(binarySupplier));
  }

  // TODO: rename
  public abstract CompletableFuture<Void> upload0(BinarySupplier binarySupplier);

  public CompletableFuture<BinarySupplier> download() {
    return objectDownloadPolicy.test(this)
      .thenAccept(allowIfPassed())
      .thenCompose(__ -> binaryRepository.findBinary(this));
  }

  public abstract boolean isInitial();

  public abstract boolean isPending();

  public abstract boolean isFulfilled();

  public long getSize() {
    return attributes.getSize().getValue();
  }
}
