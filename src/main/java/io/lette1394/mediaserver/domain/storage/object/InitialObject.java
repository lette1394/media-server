package io.lette1394.mediaserver.domain.storage.object;

import java.util.concurrent.CompletableFuture;
import lombok.Builder;

public class InitialObject extends Object {

  @Builder
  public InitialObject(
    Identifier identifier,
    Attributes attributes,
    Storage storage,
    ObjectUploadPolicy objectUploadPolicy,
    ObjectDownloadPolicy objectDownloadPolicy) {
    super(identifier, attributes, storage, objectUploadPolicy, objectDownloadPolicy);
  }

  @Override
  public CompletableFuture<Void> upload0(BinarySupplier binarySupplier) {
    return storage.createBinary(this, binarySupplier);
  }

  @Override
  public boolean isInitial() {
    return true;
  }

  @Override
  public boolean isPending() {
    return false;
  }

  @Override
  public boolean isFulfilled() {
    return false;
  }
}
