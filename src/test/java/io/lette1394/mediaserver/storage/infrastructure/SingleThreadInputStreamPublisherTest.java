package io.lette1394.mediaserver.storage.infrastructure;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class SingleThreadInputStreamPublisherTest {

  @Test
  void test1() {
    final byte[] bytes = new byte[]{1, 2, 3, 4, 5};

    StepVerifier
      .create(new SingleThreadInputStreamPublisher(new ByteArrayInputStream(bytes), 2))
      .expectNext(ByteBuffer.wrap(new byte[] {1,2}))
      .expectNext(ByteBuffer.wrap(new byte[] {3,4}))
      .expectNext(ByteBuffer.wrap(new byte[] {5}))
      .verifyComplete();
  }
}