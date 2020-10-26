package io.lette1394.mediaserver.storage.infrastructure.awss3;

import io.lette1394.mediaserver.storage.domain.Storage;
import io.lette1394.mediaserver.storage.domain.binary.LengthAwareBinarySupplier;
import io.lette1394.mediaserver.storage.domain.object.Identifier;
import io.lette1394.mediaserver.storage.domain.object.Object;
import java.util.concurrent.CompletableFuture;
import lombok.Value;

@Value
public class AwsS3Storage implements Storage<LengthAwareBinarySupplier> {
  AwsClient client;

  @Override
  public CompletableFuture<Boolean> objectExists(Identifier identifier) {
    return null;
  }

  @Override
  public CompletableFuture<Object> findObject(Identifier identifier) {
    return null;
  }

  @Override
  public CompletableFuture<Object> saveObject(Object object) {
    return null;
  }

  @Override
  public CompletableFuture<Void> deleteObject(Identifier identifier) {
    return null;
  }

  @Override
  public CompletableFuture<LengthAwareBinarySupplier> findBinary(
    Identifier identifier) {
    return CompletableFuture.failedFuture(new RuntimeException());
  }

  @Override
  public CompletableFuture<Void> saveBinary(Identifier identifier,
    LengthAwareBinarySupplier binarySupplier) {
    return client.put(new AwsObjectPath(identifier), binarySupplier);
  }

  @Override
  public CompletableFuture<Void> appendBinary(Identifier identifier,
    LengthAwareBinarySupplier binarySupplier) {
    return null;
  }

  @Override
  public CompletableFuture<Void> deleteBinary(Identifier identifier) {
    return null;
  }
}
