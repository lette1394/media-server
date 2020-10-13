package io.lette1394.mediaserver.domain.storage.usecase;

import io.lette1394.mediaserver.common.Result;
import io.lette1394.mediaserver.domain.storage.Identifier;
import io.lette1394.mediaserver.domain.storage.Object;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

class StorageFactory {
  private static final AsyncStorage storage = new InMemoryAsyncStorage();

  static AsyncStorage create() {
    return storage;
  }

  static Storage create(DataSupplier dataSupplier) {
    return storage;
  }

  public static class InMemoryAsyncStorage implements AsyncStorage {
    Map<Identifier, Object> objectHolder = new ConcurrentHashMap<>();
    Map<Identifier, byte[]> binaryHolder = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<Object> findAsync(Identifier identifier)
        throws ObjectNotFoundException {
      if (objectHolder.containsKey(identifier)) {
        return CompletableFuture.completedFuture(objectHolder.get(identifier));
      }

      throw new ObjectNotFoundException(
          String.format("Cannot found object with identifier: %s", identifier));
    }

    @Override
    public CompletableFuture<Result> uploadAsync(Object object) {
      objectHolder.put(object.getIdentifier(), object);
      binaryHolder.put(object.getIdentifier(), new byte[] {1, 2, 3, 4, 5, 60, 70, 80, 90, 100});
      return CompletableFuture.completedFuture(Result.succeed());
    }

    @Override
    public DataSupplier download(Object object) {
      final InputStream inputStream =
          new ByteArrayInputStream(binaryHolder.get(object.getIdentifier()));

      return () ->
          new InputStream() {
            @Override
            public int read() throws IOException {
              return inputStream.read();
            }
          };
    }

    @Override
    public AsyncDataSupplier downloadAsync(Object object) {
      final byte[] bytes = binaryHolder.get(object.getIdentifier());

      return new AsyncDataSupplier() {
        @Override
        public Publisher<ByteBuffer> getAsync() {
          return new Publisher<>() {
            Subscriber<? super ByteBuffer> subscriber;
            boolean isCanceled = false;

            Subscription subscription =
                new Subscription() {
                  @Override
                  public void request(long n) {
                    handler.run();
                  }

                  @Override
                  public void cancel() {
                    isCanceled = true;
                  }
                };

            Runnable handler =
                () -> {
                  for (int i = 0; i < bytes.length && !isCanceled; i++) {
                    subscriber.onNext(ByteBuffer.wrap(bytes, i, 1));
                  }
                  subscriber.onComplete();
                };

            @Override
            public void subscribe(Subscriber<? super ByteBuffer> subscriber) {
              this.subscriber = subscriber;
              subscriber.onSubscribe(subscription);
            }
          };
        }
      };
    }
  }
}
