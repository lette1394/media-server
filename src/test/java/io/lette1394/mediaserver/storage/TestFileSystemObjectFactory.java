package io.lette1394.mediaserver.storage;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import io.lette1394.mediaserver.storage.domain.object.Object;
import lombok.Value;

@Value
public class TestFileSystemObjectFactory {
  private static final String BASE_DIR = "out/binaries";
//  private static final Factory factory;

  static {
//    factory = new Factory(new FileSystemBinaryRepository(BASE_DIR));
  }

  public static Object createObjectWithRandomIdentifier() {
//    return factory.create(randomAlphanumeric(5), randomAlphanumeric(5));
    return null;
  }
}
