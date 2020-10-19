package io.lette1394.mediaserver.domain.storage.object;

import java.util.concurrent.CompletableFuture;

public interface BinaryRepository {
  default CompletableFuture<Boolean> binaryExists(Object object) {
    return findBinary(object)
      .thenApply(__ -> true)
      .exceptionally(__ -> false);
  }

  CompletableFuture<BinarySupplier> findBinary(Object object);

  CompletableFuture<Void> createBinary(Object object, BinarySupplier binarySupplier);

  CompletableFuture<Void> appendBinary(Object object, BinarySupplier binarySupplier);
}
