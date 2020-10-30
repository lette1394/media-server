package io.lette1394.mediaserver.storage.domain;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomUtils.nextBytes;
import static org.apache.commons.lang3.RandomUtils.nextInt;

import io.lette1394.mediaserver.storage.BrokenBinarySupplier;
import io.lette1394.mediaserver.storage.InMemoryStorage;
import io.lette1394.mediaserver.storage.TestBinarySupplier;
import io.vavr.control.Try;
import java.util.function.Function;
import org.reactivestreams.Publisher;

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

  public static <T, R extends SizeAware> Object<R> anyObject(
    ObjectPolicy objectPolicy,
    BinaryPolicy binaryPolicy,
    Publisher<T> publisher,
    Function<T, R> mapper) {
    final Identifier identifier = anyIdentifier();
    return new ObjectFactory<R>(objectPolicy, binaryPolicy)
      .create(identifier.getArea(), identifier.getKey(), publisher, mapper);
  }

  public static <T extends SizeAware> Object<T> anyObject(ObjectPolicy objectPolicy) {
    return new ObjectFactory<T>(
      objectPolicy,
      anyBinaryPolicy())
      .create("null", "null", anyBinary(), null);
  }

  public static <T extends SizeAware> Publisher<T> anyBinary() {
    return s -> {
      throw new RuntimeException();
    };
  }

  public static BinaryPolicy anyBinaryPolicy() {
    return binarySnapshot -> {
      throw new RuntimeException();
    };
  }

  public static <T extends SizeAware> Object<T> anyObject() {
    final ObjectPolicy allowAll = current -> Try.success(null);
    final Identifier identifier = anyIdentifier();
    final ObjectFactory<T> objectFactory = new ObjectFactory<>(
      allowAll,
      anyBinaryPolicy());
    return objectFactory.create(
      identifier.getArea(),
      identifier.getKey(),
      anyBinary(),
      null);
  }

  public static <T extends SizeAware> Object<T> the(Object<T> object) {
    return object;
  }
}
