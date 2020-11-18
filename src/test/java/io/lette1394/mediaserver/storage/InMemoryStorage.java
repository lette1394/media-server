package io.lette1394.mediaserver.storage;

import static io.lette1394.mediaserver.common.Violations.Code.INVALID_IDENTIFIER;
import static io.lette1394.mediaserver.common.Violations.violation;
import static io.lette1394.mediaserver.storage.domain.BinaryPublisher.adapt;
import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;

import io.lette1394.mediaserver.storage.domain.BinaryPath;
import io.lette1394.mediaserver.storage.domain.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.BinaryPublisher;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectNotFoundException;
import io.lette1394.mediaserver.storage.domain.ObjectRepository;
import io.lette1394.mediaserver.storage.domain.Payload;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

public abstract class InMemoryStorage<T extends Payload> implements
  ObjectRepository<T>,
  BinaryRepository<T> {

  public Map<Identifier, Object<T>> objectHolder = new ConcurrentHashMap<>();
  public Map<String, ByteArrayOutputStream> binaryHolder = new ConcurrentHashMap<>();

  @Override
  public CompletableFuture<Boolean> exists(Identifier identifier)
    throws ObjectNotFoundException {
    if (objectHolder.containsKey(identifier)) {
      return completedFuture(true);
    }
    return completedFuture(false);
  }

  @Override
  public CompletableFuture<Object<T>> find(Identifier identifier)
    throws ObjectNotFoundException {
    if (objectHolder.containsKey(identifier)) {
      return completedFuture(objectHolder.get(identifier));
    }
    return failedFuture(violation(INVALID_IDENTIFIER, format("Cannot found object with identifier: %s", identifier)));
  }

  @Override
  public CompletableFuture<Object<T>> save(Object<T> object) {
    return find(object.getIdentifier())
      .thenApply(found -> objectHolder.put(found.getIdentifier(), object));
  }

  @Override
  public CompletableFuture<Void> delete(Identifier identifier) {
    return exists(identifier).thenRun(() -> objectHolder.remove(identifier));
  }

  @Override
  public CompletableFuture<Void> create(BinaryPath binaryPath,
    BinaryPublisher<T> binaryPublisher) {
    CompletableFuture<Void> ret = new CompletableFuture<>();
    Flux
      .from(binaryPublisher)
      .doOnSubscribe(__ -> binaryHolder.put(binaryPath.asString(), new ByteArrayOutputStream()))
      .doOnComplete(() -> ret.complete(null))
      .doOnError(e -> ret.completeExceptionally(e))
      .subscribe(payload -> write(binaryPath, payload));

    return ret;
  }

  @Override
  public CompletableFuture<Void> append(BinaryPath binaryPath,
    BinaryPublisher<T> binaryPublisher) {
    final CompletableFuture<Void> ret = new CompletableFuture<>();
    Flux.from(binaryPublisher)
      .doOnComplete(() -> ret.complete(null))
      .doOnError(e -> ret.completeExceptionally(e))
      .subscribe(payload -> write(binaryPath, payload));
    return ret;
  }

  @Override
  public CompletableFuture<BinaryPublisher<T>> find(BinaryPath binaryPath) {
    return completedFuture(adapt(read(binaryPath)));
  }

  @Override
  public CompletableFuture<Void> delete(BinaryPath binaryPath) {
    binaryHolder.remove(binaryPath.asString());
    return completedFuture(null);
  }

  protected abstract Publisher<T> read(BinaryPath binaryPath);

  protected abstract void write(BinaryPath binaryPath, T item);
}
