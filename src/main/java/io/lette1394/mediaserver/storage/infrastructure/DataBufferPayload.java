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
  public void release() {
    DataBufferUtils.release(value);
  }

  @Override
  public void retain() {
    DataBufferUtils.retain(value);
  }

  @Override
  public void retain(int count) {
    for (int i = 0; i < count; i++) {
      retain();
    }
  }
}
