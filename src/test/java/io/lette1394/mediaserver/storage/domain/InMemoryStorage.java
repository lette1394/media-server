package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.storage.domain.BinaryFactory.StringLike;
import java.util.concurrent.CompletableFuture;
import lombok.Value;
import reactor.core.publisher.Flux;

@Value
public class InMemoryStorage<T extends StringLike> implements BinaryRepository<T> {

  int chunkSize;

  public InMemoryStorage(int chunkSize) {
    this.chunkSize = chunkSize;
  }

  public InMemoryStorage() {
    this(1024);
  }

  @Override
  public CompletableFuture<? extends BinarySupplier<T>> find(
    BinaryPath binaryPath) {
    return null;
  }

  @Override
  public CompletableFuture<Void> save(BinaryPath key,
    BinarySupplier<? extends T> binarySupplier) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    final Flux<? extends StringLike> from = Flux.from(binarySupplier.getAsync());
    from
      .doOnComplete(() -> future.complete(null))
      .subscribe(o -> System.out.println(o.getValue()));
    return future;
  }

  @Override
  public CompletableFuture<Void> append(BinaryPath key,
    BinarySupplier<? extends T> binarySupplier) {
    return null;
  }

  @Override
  public CompletableFuture<Void> delete(BinaryPath key) {
    return null;
  }
}
