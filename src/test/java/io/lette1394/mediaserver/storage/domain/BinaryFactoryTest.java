package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.storage.infrastructure.StringAware;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

class BinaryFactoryTest {

  @Test
  void test1() {

//    final BinaryFactory binaryFactory = new BinaryFactory();
//    final BinaryRepository<StringAware> stringLikeInMemoryStorage = new InMemoryStorage(10);
//
//    final Binary<? extends SizeAware> make = InitialBinary.<StringAware>builder()
//      .binaryRepository(stringLikeInMemoryStorage)
//      .binarySupplier(() -> Flux.fromStream(Stream.of("hello", "world", "wow", "gogo")
//        .map(str -> new StringAware(str))))
//      .build();
//
//    final BinarySupplier<StringAware> supplier = () -> Flux
//      .fromStream(Stream.of("hello", "world", "wow", "gogo")
//        .map(str -> new StringAware(str)));
//
//    final Publisher<StringAware> async = supplier.getAsync();
//
//    make.upload(new BinaryPath()).join();
  }
}