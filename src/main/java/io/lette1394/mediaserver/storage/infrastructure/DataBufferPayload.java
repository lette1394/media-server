package io.lette1394.mediaserver.storage.infrastructure;

import io.lette1394.mediaserver.storage.domain.Payload;
import lombok.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;

@Value
public class DataBufferPayload implements Payload {
  DataBuffer value;

  @Override
  public long getSize() {
    return value.readableByteCount();
  }

  @Override
  public DataBufferPayload release() {
    DataBufferUtils.release(value);
    return this;
  }

  @Override
  public DataBufferPayload retain() {
    DataBufferUtils.retain(value);
    return this;
  }
}
