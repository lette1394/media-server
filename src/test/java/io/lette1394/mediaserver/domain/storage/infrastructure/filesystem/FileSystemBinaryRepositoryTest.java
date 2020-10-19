package io.lette1394.mediaserver.domain.storage.infrastructure.filesystem;

import io.lette1394.mediaserver.domain.storage.infrastructure.SingleThreadInputStreamPublisher;
import io.lette1394.mediaserver.domain.storage.object.BinaryRepository;
import io.lette1394.mediaserver.domain.storage.object.BinarySupplier;
import io.lette1394.mediaserver.domain.storage.object.Object;
import io.lette1394.mediaserver.domain.storage.object.ObjectFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Flow.Publisher;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FileSystemBinaryRepositoryTest {
  private final static int CHUNK_SIZE = 20;

  private final static byte[] testBinary = "hello world! wow!!".getBytes();

  @Test
  @SneakyThrows
  void test1() {
    final BinaryRepository binaryRepository = new FileSystemBinaryRepository("hello");
    final ObjectFactory factory = new ObjectFactory(binaryRepository);
    final Object object = factory.create("test", "001");

    binaryRepository.createBinary(object, asyncSupplier()).join();

    byte[] holder = new byte[200];
    final int read = binaryRepository.findBinary(object).join()
      .getSync().read(holder);

    System.out.println(Arrays.toString(holder));
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
        return new ByteArrayInputStream(testBinary);
      }

      @Override
      public Publisher<ByteBuffer> getAsync() {
        return new SingleThreadInputStreamPublisher(new ByteArrayInputStream(testBinary), CHUNK_SIZE);
      }
    };
  }
}