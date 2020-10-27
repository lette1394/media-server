package io.lette1394.mediaserver.storage.domain;

import com.google.common.base.Supplier;
import io.lette1394.mediaserver.storage.domain.BinaryFactory.StringLike;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

class BinaryFactoryTest {

  @Test
  void test1() {
    final BinaryFactory binaryFactory = new BinaryFactory();
    final Binary make = binaryFactory
      .make(
        new InMemoryStorage<>(10),
        () -> Flux.fromStream(Stream.of("hello", "world", "wow", "gogo")
          .map(str -> new StringLike(str))));

    make.upload(new BinaryPath()).join();
  }
}