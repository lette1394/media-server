package io.lette1394.mediaserver.domain.storage.usecase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import io.lette1394.mediaserver.domain.storage.object.BinarySupplier;
import io.lette1394.mediaserver.domain.storage.object.Object;
import io.lette1394.mediaserver.domain.storage.object.ObjectFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Flow.Publisher;
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
    final StorageResult<Void> upload = object.upload(syncSupplier());
    upload.join();
    assertThat(upload.isDone(), is(true));
    assertThat(upload.isCompletedExceptionally(), is(false));

    final StorageResult<BinarySupplier> download = object.download();

    final byte[] downloadedBinary =
        new ByteBufferToByteArrayAsyncReader(ITEM_LENGTH).read(download.join().getAsync()).join();
    assertThat(downloadedBinary, is(testBinary));
  }

  private void runAsync(ObjectFactory factory) {
    final Object object = factory.create("1", "2");
    final StorageResult<Void> upload = object.upload(asyncSupplier());
    upload.join();
    assertThat(upload.isDone(), is(true));
    assertThat(upload.isCompletedExceptionally(), is(false));

    final StorageResult<BinarySupplier> download = object.download();

    final byte[] downloadedBinary =
      new ByteBufferToByteArrayAsyncReader(ITEM_LENGTH).read(download.join().getAsync()).join();
    assertThat(downloadedBinary, is(testBinary));
  }

  private BinarySupplier syncSupplier() {
    return new BinarySupplier() {
      @Override
      public boolean isSyncSupported() {
        return true;
      }

      @Override
      public boolean isAsyncSupported() {
        return false;
      }

      @Override
      public InputStream getSync() {
        return new ByteArrayInputStream(testBinary);
      }

      @Override
      public Publisher<ByteBuffer> getAsync() {
        throw new UnsupportedOperationException();
      }
    };
  }

  private BinarySupplier asyncSupplier() {
    return new BinarySupplier() {
      @Override
      public boolean isSyncSupported() {
        return false;
      }

      @Override
      public boolean isAsyncSupported() {
        return true;
      }

      @Override
      public InputStream getSync() {
        throw new UnsupportedOperationException();
      }

      @Override
      public Publisher<ByteBuffer> getAsync() {
        return new SingleThreadInputStreamPublisher(new ByteArrayInputStream(testBinary), CHUNK_SIZE);
      }
    };
  }
}
