package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.storage.infrastructure.StringAware;
import java.util.concurrent.CompletableFuture;
import lombok.Value;
import reactor.core.publisher.Flux;

@Value
public class InMemoryStorage implements BinaryRepository<StringAware> {

  int chunkSize;

  public InMemoryStorage(int chunkSize) {
    this.chunkSize = chunkSize;
  }

  public InMemoryStorage() {
    this(1024);
  }

  @Override
  public CompletableFuture<? extends BinarySupplier<StringAware>> findBinary(
    Identifier identifier) {
    return null;
  }

  @Override
  public CompletableFuture<Void> saveBinary(Identifier identifier,
    BinarySupplier<StringAware> binarySupplier) {
    return null;
  }

  @Override
  public CompletableFuture<Void> appendBinary(Identifier identifier,
    BinarySupplier<StringAware> binarySupplier) {
    return null;
  }

  @Override
  public CompletableFuture<Void> deleteBinary(Identifier identifier) {
    return null;
  }
}
