package io.lette1394.mediaserver.storage.domain;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomUtils.nextBytes;
import static org.apache.commons.lang3.RandomUtils.nextInt;

import io.lette1394.mediaserver.storage.BrokenBinarySupplier;
import io.lette1394.mediaserver.storage.InMemoryStorage;
import io.lette1394.mediaserver.storage.TestBinarySupplier;
import io.lette1394.mediaserver.storage.domain.object.Identifier;
import io.lette1394.mediaserver.storage.domain.object.Object;
import io.lette1394.mediaserver.storage.domain.object.ObjectFactory;
import io.lette1394.mediaserver.storage.domain.object.Policy;
import io.vavr.control.Try;

public class ObjectFixture {
  public static Identifier anyIdentifier() {
    return new Identifier(randomAlphanumeric(5), randomAlphanumeric(5));
  }

  public static BinaryRepository memory() {
    return new InMemoryStorage();
  }

  public static BinarySupplier randomBinarySupplier() {
    return new TestBinarySupplier(nextBytes(nextInt(1024, 1024 * 10 + 1)));
  }

  public static BinarySupplier brokenBinarySupplier() {
    final int start = 1024;
    final int size = nextInt(start, start * 10);

    final BinarySupplier binarySupplier = new TestBinarySupplier(nextBytes(size + 1));
    return new BrokenBinarySupplier(binarySupplier, nextInt(start, size));
  }

  public static Object anyObject(Policy policy) {
    final Identifier identifier = anyIdentifier();
    return new ObjectFactory(memory(), policy)
      .create(identifier.getArea(), identifier.getKey());
  }

  public static Object anyObject() {
    final Policy allow = current -> Try.success(null);
    final Identifier identifier = anyIdentifier();
    return new ObjectFactory(memory(), allow).create(identifier.getArea(), identifier.getKey());
  }

  public static Object the(Object object) {
    return object;
  }
}
