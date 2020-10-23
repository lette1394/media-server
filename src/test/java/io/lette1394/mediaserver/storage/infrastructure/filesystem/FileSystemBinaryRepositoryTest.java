package io.lette1394.mediaserver.storage.infrastructure.filesystem;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import io.lette1394.mediaserver.storage.domain.AutoClosableBinaryRepository;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectFactory;
import io.lette1394.mediaserver.storage.TestBinarySupplier;
import io.lette1394.mediaserver.storage.domain.DeleteAllBinaryWhenClosedBinaryRepository;
import java.io.InputStream;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    final ObjectFactory factory = new ObjectFactory(binaryRepository);
    final Object object = factory.create("test", "001");
    final byte[] binary = RandomUtils.nextBytes(CHUNK);

    binaryRepository
      .createBinary(object.identifier, new TestBinarySupplier(binary))
      .join();

    final byte[] holder = new byte[CHUNK];
    final InputStream inputStream = binaryRepository
      .findBinary(object.identifier)
      .join()
      .get()
      .getSync();
    final int readLength = inputStream.read(holder);
    final int expectEOF = inputStream.read();

    assertThat(readLength, is(CHUNK));
    assertThat(expectEOF, is(EOF));
    assertThat(holder, is(binary));
  }
}