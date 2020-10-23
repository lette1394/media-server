package io.lette1394.mediaserver.domain.storage.infrastructure.awss3;

import io.lette1394.mediaserver.common.Result;
import io.lette1394.mediaserver.domain.storage.object.BinarySupplier;
import io.lette1394.mediaserver.domain.storage.object.Identifier;
import io.lette1394.mediaserver.domain.storage.object.Object;
import io.lette1394.mediaserver.domain.storage.object.Storage;
import java.util.concurrent.CompletableFuture;
import lombok.Value;

@Value
public class AwsS3Storage implements Storage {
  AwsClient client;

  @Override
  public CompletableFuture<Result<Boolean>> objectExists(Identifier identifier) {
    return null;
  }

  @Override
  public CompletableFuture<Result<Object>> findObject(Identifier identifier) {
    return null;
  }

  @Override
  public CompletableFuture<Result<Void>> createObject(Object object) {
    return null;
  }

  @Override
  public CompletableFuture<Result<BinarySupplier>> findBinary(
    Identifier identifier) {
    return CompletableFuture.failedFuture(new RuntimeException());
  }

  @Override
  public CompletableFuture<Result<Void>> createBinary(Identifier identifier,
    BinarySupplier binarySupplier) {
    return client.put(new AwsObjectPath(identifier), binarySupplier);
  }

  @Override
  public CompletableFuture<Result<Void>> appendBinary(Identifier identifier,
    BinarySupplier binarySupplier) {
    return null;
  }

  @Override
  public CompletableFuture<Result<Void>> deleteBinary(Identifier identifier) {
    return null;
  }
}
