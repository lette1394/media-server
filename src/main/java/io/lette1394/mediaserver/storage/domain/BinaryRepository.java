package io.lette1394.mediaserver.storage.domain;

import java.util.concurrent.CompletableFuture;

public interface BinaryRepository<BUFFER extends Payload> {

  default CompletableFuture<Boolean> binaryExists(Identifier identifier) {
    return findBinary(identifier)
      .thenApply(__ -> true)
      .exceptionally(__ -> false);
  }

  CompletableFuture<BinarySupplier<BUFFER>> findBinary(Identifier identifier);

  CompletableFuture<Void> saveBinary(Identifier identifier, BinarySupplier<BUFFER> binarySupplier);

  CompletableFuture<Void> appendBinary(Identifier identifier, BinarySupplier<BUFFER> binarySupplier);

  CompletableFuture<Void> deleteBinary(Identifier identifier);

  default CompletableFuture<Void> create(BinaryPath binaryPath, BinarySupplier<BUFFER> binarySupplier) {
    throw new UnsupportedOperationException();
  }
}
