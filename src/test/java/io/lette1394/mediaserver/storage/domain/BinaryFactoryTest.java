package io.lette1394.mediaserver.storage.domain;

import com.google.common.base.Supplier;
import io.lette1394.mediaserver.storage.domain.BinaryFactory.StringLike;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

class BinaryFactoryTest {

  @Test
  void test1() {

    final BinaryFactory binaryFactory = new BinaryFactory();
    final BinaryRepository<StringLike> stringLikeInMemoryStorage = new InMemoryStorage(10);

    final Binary<? extends SizeAware> make = InitialBinary.<StringLike>builder()
      .binaryRepository(stringLikeInMemoryStorage)
      .binarySupplier(() -> Flux.fromStream(Stream.of("hello", "world", "wow", "gogo")
        .map(str -> new StringLike(str))))
      .build();

    final BinarySupplier<StringLike> supplier = () -> Flux
      .fromStream(Stream.of("hello", "world", "wow", "gogo")
        .map(str -> new StringLike(str)));

    final Publisher<StringLike> async = supplier.getAsync();

    make.upload(new BinaryPath()).join();
  }
}