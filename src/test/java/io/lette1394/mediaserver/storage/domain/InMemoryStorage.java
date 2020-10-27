package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.storage.domain.BinaryFactory.StringLike;
import java.util.concurrent.CompletableFuture;
import lombok.Value;
import reactor.core.publisher.Flux;

@Value
public class InMemoryStorage implements BinaryRepository<StringLike> {

  int chunkSize;

  public InMemoryStorage(int chunkSize) {
    this.chunkSize = chunkSize;
  }

  public InMemoryStorage() {
    this(1024);
  }

  @Override
  public CompletableFuture<BinarySupplier<StringLike>> find(
    BinaryPath binaryPath) {
    return null;
  }

  @Override
  public CompletableFuture<Void> save(BinaryPath key,
    BinarySupplier<StringLike> binarySupplier) {
    CompletableFuture<Void> future = new CompletableFuture<>();

    final Flux<StringLike> from1 = Flux.from(binarySupplier.getAsync());
    from1.subscribe(t -> t.getValue());

    final Flux<StringLike> from = Flux.from(binarySupplier.getAsync());
    from
      .doOnComplete(() -> future.complete(null))
      .subscribe(o -> System.out.println(o.getValue()));
    return future;
  }

  @Override
  public CompletableFuture<Void> append(BinaryPath key,
    BinarySupplier<StringLike> binarySupplier) {
    return null;
  }

  @Override
  public CompletableFuture<Void> delete(BinaryPath key) {
    return null;
  }
}
