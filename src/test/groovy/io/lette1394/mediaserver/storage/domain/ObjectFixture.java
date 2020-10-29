package io.lette1394.mediaserver.storage.domain;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomUtils.nextBytes;
import static org.apache.commons.lang3.RandomUtils.nextInt;

import io.lette1394.mediaserver.storage.BrokenBinarySupplier;
import io.lette1394.mediaserver.storage.InMemoryStorage;
import io.lette1394.mediaserver.storage.TestBinarySupplier;
import io.vavr.control.Try;
import java.util.function.Function;
import javax.websocket.Decoder.Binary;
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

  public static Object anyObject() {
    final ObjectPolicy allow = current -> Try.success(null);
    final Identifier identifier = anyIdentifier();
//    return new Factory(memory(), allow).create(identifier.getArea(), identifier.getKey());

    return null;
  }

  public static <T extends SizeAware> Object<T> the(Object<T> object) {
    return object;
  }
}
