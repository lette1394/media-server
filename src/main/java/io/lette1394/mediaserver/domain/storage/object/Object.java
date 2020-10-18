package io.lette1394.mediaserver.domain.storage.object;

import java.util.concurrent.CompletableFuture;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

// TODO: entity 표현
public class Object {
  // TODO: getter를 없앨 수는 없나?
  @Getter private final Identifier identifier;
  private final Attributes attributes;

  private final Storage storage;
  private final ObjectUploadPolicy objectUploadPolicy;

  @Builder(access = AccessLevel.PACKAGE)
  public Object(
      Identifier identifier,
      Attributes attributes,
      Storage storage,
      ObjectUploadPolicy objectUploadPolicy) {
    this.identifier = identifier;
    this.attributes = attributes;
    this.storage = storage;
    this.objectUploadPolicy = objectUploadPolicy;
  }

  public CompletableFuture<Void> upload(BinarySupplier binarySupplier) {
    if (objectUploadPolicy.test(this, storage)) {
      return storage
          .isExist(this.identifier)
          .thenCompose(
              isExist -> {
                if (isExist) {
                  return storage.append(this, binarySupplier);
                }
                return storage.create(this, binarySupplier);
              });
    }
    return CompletableFuture.failedFuture(new RuntimeException());
  }

  public CompletableFuture<BinarySupplier> download() {
    return storage.findBinary(this);
  }
}
