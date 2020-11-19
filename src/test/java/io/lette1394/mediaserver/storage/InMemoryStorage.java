package io.lette1394.mediaserver.storage;

import static io.lette1394.mediaserver.storage.domain.BinaryPublisher.adapt;
import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;

import io.lette1394.mediaserver.storage.domain.BinaryPath;
import io.lette1394.mediaserver.storage.domain.BinaryPublisher;
import io.lette1394.mediaserver.storage.domain.BinaryRepository;
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

public abstract class InMemoryStorage<P extends Payload> implements
  ObjectRepository<P>,
  BinaryRepository<P> {

  private final Map<Identifier, Object<P>> objectHolder = new ConcurrentHashMap<>();
  private final Map<BinaryPath, ByteArrayOutputStream> binaryHolder = new ConcurrentHashMap<>();

  public void addObject(Object<P> object) {
    objectHolder.put(object.getIdentifier(), object);
  }

  public Object<P> getObject(Identifier identifier) {
    return objectHolder.get(identifier);
  }

  public void addBinary(BinaryPath binaryPath, byte[] bytes) {
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    outputStream.writeBytes(bytes);
    binaryHolder.put(binaryPath, outputStream);
  }

  public void appendBinary(BinaryPath binaryPath, byte[] bytes) {
    binaryHolder.get(binaryPath).writeBytes(bytes);
  }

  public byte[] getBinary(BinaryPath binaryPath) {
    return binaryHolder.get(binaryPath).toByteArray();
  }

  @Override
  public CompletableFuture<Boolean> exists(Identifier identifier)
    throws ObjectNotFoundException {
    if (objectHolder.containsKey(identifier)) {
      return completedFuture(true);
    }
    return completedFuture(false);
  }

  @Override
  public CompletableFuture<Object<P>> find(Identifier identifier)
    throws ObjectNotFoundException {
    if (objectHolder.containsKey(identifier)) {
      return completedFuture(objectHolder.get(identifier));
    }
    return failedFuture(
      new ObjectNotFoundException(format("Cannot found object with identifier: %s", identifier)));
  }

  @Override
  public CompletableFuture<Object<P>> save(Object<P> object) {
    objectHolder.put(object.getIdentifier(), object);
    return CompletableFuture.completedFuture(object);
  }

  @Override
  public CompletableFuture<Void> delete(Identifier identifier) {
    return exists(identifier).thenRun(() -> objectHolder.remove(identifier));
  }

  @Override
  public CompletableFuture<Void> create(BinaryPath binaryPath,
    BinaryPublisher<P> binaryPublisher) {
    CompletableFuture<Void> ret = new CompletableFuture<>();
    Flux
      .from(binaryPublisher)
      .doOnSubscribe(__ -> binaryHolder.put(binaryPath, new ByteArrayOutputStream()))
      .doOnComplete(() -> ret.complete(null))
      .doOnError(e -> ret.completeExceptionally(e))
      .subscribe(payload -> write(binaryPath, payload));

    return ret;
  }

  @Override
  public CompletableFuture<Void> append(BinaryPath binaryPath,
    BinaryPublisher<P> binaryPublisher) {
    final CompletableFuture<Void> ret = new CompletableFuture<>();
    Flux.from(binaryPublisher)
      .doOnComplete(() -> ret.complete(null))
      .doOnError(e -> ret.completeExceptionally(e))
      .subscribe(payload -> write(binaryPath, payload));
    return ret;
  }

  @Override
  public CompletableFuture<BinaryPublisher<P>> find(BinaryPath binaryPath) {
    return completedFuture(adapt(read(binaryPath)));
  }

  @Override
  public CompletableFuture<Void> delete(BinaryPath binaryPath) {
    binaryHolder.remove(binaryPath);
    return completedFuture(null);
  }

  protected abstract Publisher<P> read(BinaryPath binaryPath);

  protected abstract void write(BinaryPath binaryPath, P item);
}
