package io.lette1394.mediaserver.domain.storage.usecase;

import static java.lang.String.format;

import io.lette1394.mediaserver.common.Result;
import io.lette1394.mediaserver.domain.storage.Identifier;
import io.lette1394.mediaserver.domain.storage.Object;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Value;

@Value
class InMemoryStorage implements Storage, AsyncStorage {
  static Map<Identifier, Object> objectHolder = new ConcurrentHashMap<>();
  static Map<Identifier, byte[]> binaryHolder = new ConcurrentHashMap<>();

  DataSupplier dataSupplier;
  int maxChunkSizeInByte;

  @Override
  public Object find(Identifier identifier) throws ObjectNotFoundException {
    return objectHolder.get(identifier);
  }

  @Override
  public CompletableFuture<Object> findAsync(Identifier identifier) {
    if (objectHolder.containsKey(identifier)) {
      return CompletableFuture.completedFuture(objectHolder.get(identifier));
    }

    throw new ObjectNotFoundException(
        format("Cannot found object with identifier: %s", identifier));
  }

  @Override
  public Result upload(Object object) {
    try {
      final byte[] bytes = readAll(dataSupplier.get());
      objectHolder.put(object.getIdentifier(), object);
      binaryHolder.put(object.getIdentifier(), bytes);

      return Result.succeed();
    } catch (IOException e) {
      return Result.fail(e);
    }
  }

  @Override
  public CompletableFuture<Result> uploadAsync(Object object) {
    final Result result = upload(object);
    if (result.isSucceed()) {
      return CompletableFuture.completedFuture(result);
    }
    return CompletableFuture.failedFuture(result.getThrowable());
  }

  @Override
  public DataSupplier download(Object object) {
    return () -> new ByteArrayInputStream(binaryHolder.get(object.getIdentifier()));
  }

  @Override
  public AsyncDataSupplier downloadAsync(Object object) {
    final byte[] bytes = binaryHolder.get(object.getIdentifier());
    return () -> new InputStreamPublisher(new ByteArrayInputStream(bytes), maxChunkSizeInByte);
  }

  private byte[] readAll(InputStream inputStream) throws IOException {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();

    while (true) {
      final byte[] bytes = new byte[maxChunkSizeInByte];
      final int length = inputStream.read(bytes);
      if (length == -1) {
        return out.toByteArray();
      }

      out.write(bytes, 0, length);
    }
  }
}
