package io.lette1394.mediaserver.storage.infrastructure;

import io.lette1394.mediaserver.storage.domain.SizeAware;
import lombok.Value;
import org.springframework.core.io.buffer.DataBuffer;

@Value
public class DataBufferPayload implements SizeAware {
  DataBuffer value;

  @Override
  public long getSize() {
    return value.readableByteCount();
  }
}
