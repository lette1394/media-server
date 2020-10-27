package io.lette1394.mediaserver.storage.domain.binary;

import io.lette1394.mediaserver.storage.domain.object.Identifier;
import java.util.concurrent.CompletableFuture;

public interface BinaryRepository {

  default CompletableFuture<Boolean> binaryExists(Identifier identifier) {
    return findBinary(identifier)
      .thenApply(__ -> true)
      .exceptionally(__ -> false);
  }

  CompletableFuture<? extends BinarySupplier> findBinary(Identifier identifier);

  CompletableFuture<Void> saveBinary(Identifier identifier, BinarySupplier binarySupplier);

  CompletableFuture<Void> appendBinary(Identifier identifier, BinarySupplier binarySupplier);

  CompletableFuture<Void> deleteBinary(Identifier identifier);
}
