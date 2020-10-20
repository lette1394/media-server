package io.lette1394.mediaserver.domain.storage;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import io.lette1394.mediaserver.domain.storage.infrastructure.filesystem.FileSystemBinaryRepository;
import io.lette1394.mediaserver.domain.storage.object.Object;
import io.lette1394.mediaserver.domain.storage.object.ObjectFactory;
import lombok.Value;

@Value
public class TestFileSystemObjectFactory {
  private static final String BASE_DIR = "out/binaries";
  private static final ObjectFactory factory;

  static {
    factory = new ObjectFactory(new FileSystemBinaryRepository(BASE_DIR));
  }

  public static Object createObjectWithRandomIdentifier() {
    return factory.create(randomAlphanumeric(5), randomAlphanumeric(5));
  }
}
