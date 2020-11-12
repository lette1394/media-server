package io.lette1394.mediaserver.processing.domain;

import io.lette1394.mediaserver.storage.domain.Payload;
import io.lette1394.mediaserver.storage.infrastructure.DataBufferPayload;
import org.springframework.core.io.buffer.DataBuffer;

@FunctionalInterface
public interface PayloadParser<B extends Payload> {
  byte[] parse(B payload);

  class DataBufferPayloadParser implements PayloadParser<DataBufferPayload> {
    @Override
    public byte[] parse(DataBufferPayload payload) {
      final DataBuffer buffer = payload.getValue();
      final byte[] ret = new byte[buffer.readableByteCount()];
      buffer.read(ret);
      return ret;
    }
  }
}
