package io.lette1394.mediaserver.storage.domain;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import io.lette1394.mediaserver.storage.InMemoryStorage;
import io.lette1394.mediaserver.storage.TestBinarySupplier;
import org.apache.commons.lang3.RandomUtils;

public class ObjectFixture {
  public static Identifier anyIdentifier() {
    return new Identifier(randomAlphanumeric(5), randomAlphanumeric(5));
  }

  public static BinaryRepository memory() {
    return new InMemoryStorage();
  }

  public static BinarySupplier randomBinarySupplier() {
    return new TestBinarySupplier(RandomUtils.nextBytes(RandomUtils.nextInt(1024, 1024 * 10 + 1)));
  }
}
