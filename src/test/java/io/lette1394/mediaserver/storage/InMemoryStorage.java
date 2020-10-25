package io.lette1394.mediaserver.storage;

import static io.lette1394.mediaserver.common.Violations.violation;
import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;

import io.lette1394.mediaserver.storage.domain.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.Storage;
import io.lette1394.mediaserver.storage.infrastructure.ByteBufferToByteArrayAsyncAggregateReader;
import io.lette1394.mediaserver.storage.infrastructure.SingleThreadInputStreamPublisher;
import io.lette1394.mediaserver.storage.usecase.ObjectNotFoundException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Value;
import org.reactivestreams.Publisher;

@Value
public class InMemoryStorage implements Storage {
  static Map<Identifier, Object> objectHolder = new ConcurrentHashMap<>();
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
  public CompletableFuture<Object> findObject(Identifier identifier)
    throws ObjectNotFoundException {
    if (objectHolder.containsKey(identifier)) {
      return completedFuture(objectHolder.get(identifier));
    }
    return failedFuture(violation(format("Cannot found object with identifier: %s", identifier)));
  }

  @Override
  public CompletableFuture<Object> saveObject(Object object) {
    return findObject(object.identifier)
      .thenApply(found -> objectHolder.put(found.identifier, object));
  }

  @Override
  public CompletableFuture<Void> deleteObject(Identifier identifier) {
    return objectExists(identifier).thenRun(() -> objectHolder.remove(identifier));
  }

  @Override
  public CompletableFuture<Void> saveBinary(Identifier identifier,
    BinarySupplier binarySupplier) {
    if (binarySupplier.isSyncSupported()) {
      return uploadBinarySync(identifier, binarySupplier);
    }
    if (binarySupplier.isAsyncSupported()) {
      return uploadBinaryAsync(identifier, binarySupplier);
    }
    throw new IllegalStateException("BinarySupplier supports nothing");
  }

  @Override
  public CompletableFuture<Void> appendBinary(Identifier identifier,
    BinarySupplier binarySupplier) {
    final byte[] bytes = binaryHolder.get(identifier);
    final InputStream input = binarySupplier.getSync();

    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      out.writeBytes(bytes);
      out.writeBytes(readAll(input));
    } catch (IOException e) {
      return failedFuture(e);
    }
    return completedFuture(null);
  }

  @Override
  public CompletableFuture<BinarySupplier> findBinary(
    Identifier identifier) {
    final byte[] binaries = binaryHolder.get(identifier);
    return completedFuture(
      new BinarySupplier() {
        @Override
        public boolean isSyncSupported() {
          return true;
        }

        @Override
        public boolean isAsyncSupported() {
          return true;
        }

        @Override
        public long getLength() {
          return binaries.length;
        }

        @Override
        public InputStream getSync() {
          return new ByteArrayInputStream(binaries);
        }

        @Override
        public Publisher<ByteBuffer> getAsync() {
          return new SingleThreadInputStreamPublisher(new ByteArrayInputStream(binaries),
            chunkSize);
        }
      });
  }

  @Override
  public CompletableFuture<Void> deleteBinary(Identifier identifier) {
    return null;
  }

  private CompletableFuture<Void> uploadBinarySync(Identifier identifier,
    BinarySupplier binarySupplier) {
    try {
      final byte[] bytes = readAll(binarySupplier.getSync());
      binaryHolder.put(identifier, bytes);

      return completedFuture(null);
    } catch (IOException e) {
      return failedFuture(e);
    }
  }

  private CompletableFuture<Void> uploadBinaryAsync(Identifier identifier,
    BinarySupplier binarySupplier) {
    return new ByteBufferToByteArrayAsyncAggregateReader(500)
      .read(binarySupplier.getAsync())
      .thenAccept(bytes -> binaryHolder.put(identifier, bytes));
  }

  private byte[] readAll(InputStream inputStream) throws IOException {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();

    while (true) {
      final byte[] bytes = new byte[chunkSize];
      final int length = inputStream.read(bytes);
      if (length == -1) {
        return out.toByteArray();
      }

      out.write(bytes, 0, length);
    }
  }
}
