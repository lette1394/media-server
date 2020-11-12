package io.lette1394.mediaserver.processing.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import io.lette1394.mediaserver.MemoryLeakTest;
import io.lette1394.mediaserver.processing.domain.PayloadParser.DataBufferPayloadParser;
import io.lette1394.mediaserver.storage.infrastructure.DataBufferPayload;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;

// TODO: rename test class
class RetainingPayloadParserTest extends MemoryLeakTest {
  @Test
  void test1() {
    final byte[] content = "hello world".getBytes(StandardCharsets.UTF_8);

    final PayloadParser<DataBufferPayload> payloadParser = new DataBufferPayloadParser();

    final DataBufferFactory factory = new NettyDataBufferFactory(
      memoryLeakDetectableByteBufAllocator);
    final DataBuffer dataBuffer = factory.allocateBuffer(content.length);
    dataBuffer.write(content);

    final byte[] parse = payloadParser.parse(new DataBufferPayload(dataBuffer));
    DataBufferUtils.release(dataBuffer);

    assertThat(parse, is(content));
  }
}