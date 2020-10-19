package io.lette1394.mediaserver.domain.storage.object;

import java.util.concurrent.CompletableFuture;
import lombok.Builder;


public class PendingObject extends Object {
  @Builder
  public PendingObject(Identifier identifier,
    Attributes attributes, Storage storage,
    ObjectUploadPolicy objectUploadPolicy,
    ObjectDownloadPolicy objectDownloadPolicy) {
    super(identifier, attributes, storage, objectUploadPolicy, objectDownloadPolicy);
  }

  @Override
  public CompletableFuture<Void> upload0(BinarySupplier binarySupplier) {
    return storage.appendBinary(this, binarySupplier);
  }

  @Override
  public boolean isInitial() {
    return false;
  }

  @Override
  public boolean isPending() {
    return true;
  }

  @Override
  public boolean isFulfilled() {
    return false;
  }
}
