package io.lette1394.mediaserver.storage.infrastructure.filesystem;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import io.lette1394.mediaserver.storage.TestBinarySupplier;
import io.lette1394.mediaserver.storage.domain.AutoClosableBinaryRepository;
import io.lette1394.mediaserver.storage.domain.DeleteAllBinaryWhenClosedBinaryRepository;
import io.lette1394.mediaserver.storage.domain.object.Factory;
import io.lette1394.mediaserver.storage.domain.object.Object;
import io.lette1394.mediaserver.storage.infrastructure.ByteBufferToByteArrayAsyncAggregateReader;
import java.nio.ByteBuffer;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;

class FileSystemBinaryRepositoryTest {

  private static final int EOF = -1;
  private static final int CHUNK = 10;
  private static final String BASE_DIR = "out/binaries";

  private AutoClosableBinaryRepository binaryRepository;

  @BeforeEach
  void beforeEach() {
    binaryRepository = new DeleteAllBinaryWhenClosedBinaryRepository(
      new FileSystemBinaryRepository(BASE_DIR));
  }

  @AfterEach
  void afterEach() {
    try {
      binaryRepository.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  @SneakyThrows
  void test1() {
//    final Factory factory = new Factory(binaryRepository);
//    final Object object = factory.create("test", "001");
//    final byte[] binary = RandomUtils.nextBytes(CHUNK);
//
//    binaryRepository
//      .saveBinary(object.getIdentifier(), new TestBinarySupplier(binary))
//      .join();
//
//    final Publisher<ByteBuffer> async = binaryRepository
//      .findBinary(object.getIdentifier())
//      .join()
//      .getAsync();
//
//    final ByteBufferToByteArrayAsyncAggregateReader reader
//      = new ByteBufferToByteArrayAsyncAggregateReader(1);
//    final byte[] afterRead = reader.read(async).join();
//
//    assertThat(afterRead.length, is(CHUNK));
//    assertThat(afterRead, is(binary));
  }
}