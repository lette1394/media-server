package io.lette1394.mediaserver.domain.storage.usecase;

import static io.lette1394.mediaserver.domain.storage.usecase.StorageResult.completed;
import static io.lette1394.mediaserver.domain.storage.usecase.StorageResult.failed;
import static io.lette1394.mediaserver.domain.storage.usecase.StorageResult.storageResult;
import static java.lang.String.format;

import io.lette1394.mediaserver.domain.storage.object.BinarySupplier;
import io.lette1394.mediaserver.domain.storage.object.Identifier;
import io.lette1394.mediaserver.domain.storage.object.Object;
import io.lette1394.mediaserver.domain.storage.object.Storage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Flow.Publisher;
import lombok.Value;

@Value
class InMemoryStorage implements Storage {
  static Map<Identifier, Object> objectHolder = new ConcurrentHashMap<>();
  static Map<Identifier, byte[]> binaryHolder = new ConcurrentHashMap<>();

  int chunkSize;

  @Override
  public StorageResult<Object> find(Identifier identifier) throws ObjectNotFoundException {
    if (objectHolder.containsKey(identifier)) {
      return completed(objectHolder.get(identifier));
    }
    return failed(
        new ObjectNotFoundException(format("Cannot found object with identifier: %s", identifier)));
  }

  @Override
  public StorageResult<Void> create(Object object, BinarySupplier binarySupplier) {
    if (binarySupplier.isSyncSupported()) {
      return uploadSync(object, binarySupplier);
    }
    if (binarySupplier.isAsyncSupported()) {
      return uploadAsync(object, binarySupplier);
    }
    throw new IllegalStateException("BinarySupplier supports nothing");
  }

  @Override
  public StorageResult<Void> append(Object object, BinarySupplier binarySupplier) {
    throw new UnsupportedOperationException();
  }

  @Override
  public StorageResult<BinarySupplier> findBinary(Object object) {
    return storageResult(
      CompletableFuture.completedFuture(
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
          public InputStream getSync() {
            return new ByteArrayInputStream(binaryHolder.get(object.getIdentifier()));
          }

          @Override
          public Publisher<ByteBuffer> getAsync() {
            final byte[] bytes = binaryHolder.get(object.getIdentifier());
            return new SingleThreadInputStreamPublisher(
              new ByteArrayInputStream(bytes), chunkSize);
          }
        }));
  }

  private StorageResult<Void> uploadSync(Object object, BinarySupplier binarySupplier) {
    try {
      final byte[] bytes = readAll(binarySupplier.getSync());
      objectHolder.put(object.getIdentifier(), object);
      binaryHolder.put(object.getIdentifier(), bytes);

      return completed();
    } catch (IOException e) {
      return failed(e);
    }
  }

  private StorageResult<Void> uploadAsync(Object object, BinarySupplier binarySupplier) {
    return storageResult(
      new ByteBufferToByteArrayAsyncReader(500)
        .read(binarySupplier.getAsync())
        .thenAccept(bytes -> {
          objectHolder.put(object.getIdentifier(), object);
          binaryHolder.put(object.getIdentifier(), bytes);
        }));

    // or just using SingleThreadedAsyncToSync
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
