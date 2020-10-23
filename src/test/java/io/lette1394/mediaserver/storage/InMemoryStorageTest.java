package io.lette1394.mediaserver.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import io.lette1394.mediaserver.common.Result;
import io.lette1394.mediaserver.storage.domain.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectFactory;
import io.lette1394.mediaserver.storage.infrastructure.ByteBufferToByteArrayAsyncAggregateReader;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

class InMemoryStorageTest {
  private final static int CHUNK_SIZE = 20;
  private final static int ITEM_LENGTH = 100;

  private final static byte[] testBinary = RandomStringUtils.random(1000).getBytes();

  @Test
  void sync() {
    runSync(new ObjectFactory(new InMemoryStorage(CHUNK_SIZE)));
  }

  @Test
  void async() {
    runAsync(new ObjectFactory(new InMemoryStorage(CHUNK_SIZE)));
  }

  private void runSync(ObjectFactory factory) {
    final Object object = factory.create("1", "2");
    final CompletableFuture<Result<Void>> upload = object.upload(new TestBinarySupplier(testBinary));
    upload.join();
    assertThat(upload.isDone(), is(true));
    assertThat(upload.isCompletedExceptionally(), is(false));

    final CompletableFuture<Result<BinarySupplier>> download = object.download();

    final Result<BinarySupplier> result = download.join();
    if (result.isFailed()) {
      throw new RuntimeException(result.getThrowable());
    }
    final byte[] downloadedBinary = new ByteBufferToByteArrayAsyncAggregateReader(ITEM_LENGTH)
      .read(result.get().getAsync())
      .join();

    assertThat(downloadedBinary, is(testBinary));
  }

  private void runAsync(ObjectFactory factory) {
    final Object object = factory.create("1", "2");
    final CompletableFuture<Result<Void>> upload = object.upload(new TestBinarySupplier(testBinary));
    upload.join();
    assertThat(upload.isDone(), is(true));
    assertThat(upload.isCompletedExceptionally(), is(false));

    final CompletableFuture<Result<BinarySupplier>> download = object.download();

    final byte[] downloadedBinary = new ByteBufferToByteArrayAsyncAggregateReader(ITEM_LENGTH)
      .read(download.join().get().getAsync())
      .join();

    assertThat(downloadedBinary, is(testBinary));
  }
}
