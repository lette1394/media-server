package io.lette1394.mediaserver.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import io.lette1394.mediaserver.storage.domain.binary.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.object.Object;
import io.lette1394.mediaserver.storage.domain.object.ObjectFactory;
import io.lette1394.mediaserver.storage.infrastructure.ByteBufferToByteArrayAsyncAggregateReader;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;

class InMemoryStorageTest {
  private final static int CHUNK_SIZE = 20;
  private final static int ITEM_LENGTH = 100;

  private final static byte[] testBinary = RandomUtils.nextBytes(1024);

  @Test
  void sync() {
    runSync(new ObjectFactory(new InMemoryStorage(CHUNK_SIZE)));
  }

  @Test
  void async() {
    runAsync(new ObjectFactory(new InMemoryStorage(CHUNK_SIZE)));
  }

  private void runSync(ObjectFactory factory) {
    final Object object = factory.create("1", "2")
      .upload(new TestBinarySupplier(testBinary))
      .join();

    final BinarySupplier download = object.download().join();

    final byte[] downloadedBinary = new ByteBufferToByteArrayAsyncAggregateReader(ITEM_LENGTH)
      .read(download.getAsync())
      .join();

    assertThat(downloadedBinary, is(testBinary));
  }

  private void runAsync(ObjectFactory factory) {
    final Object object = factory.create("1", "2")
      .upload(new TestBinarySupplier(testBinary))
      .join();
    final BinarySupplier download = object.download().join();
    final byte[] downloadedBinary = new ByteBufferToByteArrayAsyncAggregateReader(ITEM_LENGTH)
      .read(download.getAsync())
      .join();

    assertThat(downloadedBinary, is(testBinary));
  }
}
