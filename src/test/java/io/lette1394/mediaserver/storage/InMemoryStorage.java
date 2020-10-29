package io.lette1394.mediaserver.storage;

import static io.lette1394.mediaserver.common.Violations.violation;
import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;

import io.lette1394.mediaserver.storage.domain.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.LengthAwareBinarySupplier;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectRepository;
import io.lette1394.mediaserver.storage.infrastructure.ByteBufferToByteArrayAsyncAggregateReader;
import io.lette1394.mediaserver.storage.infrastructure.Publishers;
import io.lette1394.mediaserver.storage.infrastructure.SingleThreadInputStreamPublisher;
import io.lette1394.mediaserver.storage.infrastructure.ByteBufferAware;
import io.lette1394.mediaserver.storage.usecase.ObjectNotFoundException;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Value;
import org.reactivestreams.Publisher;

@Value
public class InMemoryStorage implements
  ObjectRepository<ByteBufferAware>,
  BinaryRepository<ByteBufferAware> {

  static Map<Identifier, Object<ByteBufferAware>> objectHolder = new ConcurrentHashMap<>();
  static Map<Identifier, byte[]> binaryHolder = new ConcurrentHashMap<>();

  int chunkSize;

  public InMemoryStorage(int chunkSize) {
    this.chunkSize = chunkSize;
  }

  public InMemoryStorage() {
    this(1024);
  }

  @Override
  public CompletableFuture<Boolean> objectExists(Identifier identifier)
    throws ObjectNotFoundException {
    if (objectHolder.containsKey(identifier)) {
      return completedFuture(true);
    }
    return completedFuture(false);
  }

  @Override
  public CompletableFuture<Object<ByteBufferAware>> findObject(Identifier identifier)
    throws ObjectNotFoundException {
    if (objectHolder.containsKey(identifier)) {
      return completedFuture(objectHolder.get(identifier));
    }
    return failedFuture(violation(format("Cannot found object with identifier: %s", identifier)));
  }

  @Override
  public CompletableFuture<Object<ByteBufferAware>> saveObject(Object<ByteBufferAware> object) {
    return findObject(object.getIdentifier())
      .thenApply(found -> objectHolder.put(found.getIdentifier(), object));
  }

  @Override
  public CompletableFuture<Void> deleteObject(Identifier identifier) {
    return objectExists(identifier).thenRun(() -> objectHolder.remove(identifier));
  }

  @Override
  public CompletableFuture<Void> saveBinary(Identifier identifier, BinarySupplier<ByteBufferAware> binarySupplier) {
    return uploadBinaryAsync(identifier, binarySupplier);
  }

  @Override
  public CompletableFuture<Void> appendBinary(Identifier identifier,
    BinarySupplier<ByteBufferAware> binarySupplier) {
    final byte[] bytes = binaryHolder.get(identifier);

    // TODO: implements
    return completedFuture(null);
  }

  @Override
  public CompletableFuture<BinarySupplier<ByteBufferAware>> findBinary(
    Identifier identifier) {
    final byte[] binaries = binaryHolder.get(identifier);
    return completedFuture(new LengthAwareBinarySupplier<>() {
      @Override
      public long getLength() {
        return binaries.length;
      }

      @Override
      public Publisher<ByteBufferAware> getAsync() {
        return
          Publishers.convert(new SingleThreadInputStreamPublisher(new ByteArrayInputStream(binaries),
          chunkSize), byteBuffer -> new ByteBufferAware(byteBuffer));
      }
    });
  }

  @Override
  public CompletableFuture<Void> deleteBinary(Identifier identifier) {
    binaryHolder.remove(identifier);
    return completedFuture(null);
  }

  private CompletableFuture<Void> uploadBinaryAsync(Identifier identifier,
    BinarySupplier<ByteBufferAware> binarySupplier) {
    final Publisher<ByteBuffer> convert = Publishers
      .convert(binarySupplier.getAsync(), byteBufferAware -> byteBufferAware.getValue());
    return new ByteBufferToByteArrayAsyncAggregateReader(500)
      .read(convert)
      .thenAccept(bytes -> binaryHolder.put(identifier, bytes));
  }
}
