package io.lette1394.mediaserver.storage.domain.binary;

import io.lette1394.mediaserver.storage.domain.object.Identifier;
import java.util.concurrent.CompletableFuture;

public interface BinaryRepository<T extends BinarySupplier> {
  default CompletableFuture<Boolean> binaryExists(Identifier identifier) {
    return findBinary(identifier)
      .thenApply(__ -> true)
      .exceptionally(__ -> false);
  }

  CompletableFuture<T> findBinary(Identifier identifier);

  CompletableFuture<Void> saveBinary(Identifier identifier, T binarySupplier);

  CompletableFuture<Void> appendBinary(Identifier identifier, T binarySupplier);

  CompletableFuture<Void> deleteBinary(Identifier identifier);
}
