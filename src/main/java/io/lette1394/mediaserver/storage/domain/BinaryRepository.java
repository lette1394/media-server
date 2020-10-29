package io.lette1394.mediaserver.storage.domain;

import java.util.concurrent.CompletableFuture;

public interface BinaryRepository<BUFFER extends SizeAware> {

  default CompletableFuture<Boolean> binaryExists(Identifier identifier) {
    return findBinary(identifier)
      .thenApply(__ -> true)
      .exceptionally(__ -> false);
  }

  CompletableFuture<? extends BinarySupplier<BUFFER>> findBinary(Identifier identifier);

  CompletableFuture<Void> saveBinary(Identifier identifier, BinarySupplier<BUFFER> binarySupplier);

  CompletableFuture<Void> appendBinary(Identifier identifier, BinarySupplier<BUFFER> binarySupplier);

  CompletableFuture<Void> deleteBinary(Identifier identifier);
}
