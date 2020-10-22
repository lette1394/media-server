package io.lette1394.mediaserver.domain.storage.object;

import io.lette1394.mediaserver.common.Result;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public interface BinaryRepository {
  default CompletableFuture<Result<Boolean>> binaryExists(Identifier identifier) {
    return findBinary(identifier)
      .thenApply(binary -> {
        if (Objects.nonNull(binary)) {
          return Result.succeed(true);
        }
        return Result.succeed(false);
      })
      .exceptionally(Result::fail);
  }

  CompletableFuture<Result<BinarySupplier>> findBinary(Identifier identifier);

  CompletableFuture<Result<Void>> createBinary(Identifier identifier,
    BinarySupplier binarySupplier);

  CompletableFuture<Result<Void>> appendBinary(Identifier identifier,
    BinarySupplier binarySupplier);

  CompletableFuture<Result<Void>> deleteBinary(Identifier identifier);
}
