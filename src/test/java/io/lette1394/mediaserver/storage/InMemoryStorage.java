package io.lette1394.mediaserver.storage;

import static io.lette1394.mediaserver.common.Violations.violation;
import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;

import io.lette1394.mediaserver.storage.domain.BinaryPath;
import io.lette1394.mediaserver.storage.domain.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.LengthAwareBinarySupplier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectRepository;
import io.lette1394.mediaserver.storage.infrastructure.ByteBufferPayload;
import io.lette1394.mediaserver.storage.infrastructure.Publishers;
import io.lette1394.mediaserver.storage.infrastructure.SingleThreadInputStreamPublisher;
import io.lette1394.mediaserver.storage.domain.ObjectNotFoundException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Value;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

@Value
public class InMemoryStorage implements
  ObjectRepository<ByteBufferPayload>,
  BinaryRepository<ByteBufferPayload> {

  static Map<Identifier, Object<ByteBufferPayload>> objectHolder = new ConcurrentHashMap<>();
  static Map<BinaryPath, byte[]> binaryHolder = new ConcurrentHashMap<>();

  int chunkSize;

  public InMemoryStorage(int chunkSize) {
    this.chunkSize = chunkSize;
  }

  public InMemoryStorage() {
    this(1024);
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
  public CompletableFuture<Object<ByteBufferPayload>> find(Identifier identifier)
    throws ObjectNotFoundException {
    if (objectHolder.containsKey(identifier)) {
      return completedFuture(objectHolder.get(identifier));
    }
    return failedFuture(violation(format("Cannot found object with identifier: %s", identifier)));
  }

  @Override
  public CompletableFuture<Object<ByteBufferPayload>> save(Object<ByteBufferPayload> object) {
    return find(object.getIdentifier())
      .thenApply(found -> objectHolder.put(found.getIdentifier(), object));
  }

  @Override
  public CompletableFuture<Void> delete(Identifier identifier) {
    return exists(identifier).thenRun(() -> objectHolder.remove(identifier));
  }

  @Override
  public CompletableFuture<Void> create(BinaryPath binaryPath,
    BinarySupplier<ByteBufferPayload> binarySupplier) {
    return uploadBinaryAsync(binaryPath, binarySupplier);
  }

  @Override
  public CompletableFuture<Void> append(BinaryPath binaryPath,
    BinarySupplier<ByteBufferPayload> binarySupplier) {
    final byte[] bytes = binaryHolder.get(binaryPath);

    // TODO: implements
    return completedFuture(null);
  }

  @Override
  public CompletableFuture<BinarySupplier<ByteBufferPayload>> find(BinaryPath binaryPath) {
    final byte[] binaries = binaryHolder.get(binaryPath);
    return completedFuture(new LengthAwareBinarySupplier<>() {
      @Override
      public long getLength() {
        return binaries.length;
      }

      @Override
      public Publisher<ByteBufferPayload> getAsync() {
        return
          Publishers
            .convert(new SingleThreadInputStreamPublisher(new ByteArrayInputStream(binaries),
              chunkSize), byteBuffer -> new ByteBufferPayload(byteBuffer));
      }
    });
  }

  @Override
  public CompletableFuture<Void> delete(BinaryPath binaryPath) {
    binaryHolder.remove(binaryPath);
    return completedFuture(null);
  }

  private CompletableFuture<Void> uploadBinaryAsync(BinaryPath binaryPath,
    BinarySupplier<ByteBufferPayload> binarySupplier) {

    return Flux
      .from(binarySupplier.getAsync())
      .map(payload -> payload.getValue())
      .reduce(new ByteArrayOutputStream(), (acc, cur) -> {
        byte[] bytes = new byte[cur.remaining()];
        cur.get(bytes);
        acc.writeBytes(bytes);
        return acc;
      })
      .map(stream -> stream.toByteArray())
      .toFuture()
      .thenAccept(bytes -> binaryHolder.put(binaryPath, bytes));
  }
}
