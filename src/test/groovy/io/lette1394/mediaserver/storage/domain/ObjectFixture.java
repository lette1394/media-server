package io.lette1394.mediaserver.storage.domain;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomUtils.nextBytes;
import static org.apache.commons.lang3.RandomUtils.nextInt;

import io.lette1394.mediaserver.storage.BrokenBinarySupplier;
import io.lette1394.mediaserver.storage.InMemoryStorage;
import io.lette1394.mediaserver.storage.TestBinarySupplier;
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

    final LengthAwareBinarySupplier binarySupplier = new TestBinarySupplier(nextBytes(size + 1));
    return new BrokenBinarySupplier(binarySupplier, nextInt(start, size));
  }

  public static Object anyObject(ObjectPolicy objectPolicy) {
    final Identifier identifier = anyIdentifier();
    return new ObjectFactory(objectPolicy)
      .create(identifier.getArea(), identifier.getKey());
  }

  public static Object anyObject() {
    final ObjectPolicy allow = current -> Try.success(null);
    final Identifier identifier = anyIdentifier();
//    return new Factory(memory(), allow).create(identifier.getArea(), identifier.getKey());

    return null;
  }

  public static Object the(Object object) {
    return object;
  }
}
