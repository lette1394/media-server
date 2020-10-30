package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.storage.infrastructure.StringPayload;
import java.util.concurrent.CompletableFuture;
import lombok.Value;

@Value
public class InMemoryStorage implements BinaryRepository<StringPayload> {

  int chunkSize;

  public InMemoryStorage(int chunkSize) {
    this.chunkSize = chunkSize;
  }

  public InMemoryStorage() {
    this(1024);
  }

  @Override
  public CompletableFuture<? extends BinarySupplier<StringPayload>> findBinary(
    Identifier identifier) {
    return null;
  }

  @Override
  public CompletableFuture<Void> saveBinary(Identifier identifier,
    BinarySupplier<StringPayload> binarySupplier) {
    return null;
  }

  @Override
  public CompletableFuture<Void> appendBinary(Identifier identifier,
    BinarySupplier<StringPayload> binarySupplier) {
    return null;
  }

  @Override
  public CompletableFuture<Void> deleteBinary(Identifier identifier) {
    return null;
  }
}
