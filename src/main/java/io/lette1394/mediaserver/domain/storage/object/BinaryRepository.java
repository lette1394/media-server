package io.lette1394.mediaserver.domain.storage.object;

import io.lette1394.mediaserver.common.Result;
import java.util.concurrent.CompletableFuture;

public interface BinaryRepository {
  default CompletableFuture<Boolean> binaryExists(Identifier identifier) {
    return findBinary(identifier)
      .thenApply(__ -> true)
      .exceptionally(__ -> false);
  }

  CompletableFuture<BinarySupplier> findBinary(Identifier identifier);

  CompletableFuture<Result> createBinary(Identifier identifier, BinarySupplier binarySupplier);

  CompletableFuture<Result> appendBinary(Identifier identifier, BinarySupplier binarySupplier);

  CompletableFuture<Result> deleteBinary(Identifier identifier);
}
