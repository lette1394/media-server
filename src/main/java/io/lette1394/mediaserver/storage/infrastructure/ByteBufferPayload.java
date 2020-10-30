package io.lette1394.mediaserver.storage.infrastructure;

import io.lette1394.mediaserver.storage.domain.SizeAware;
import java.nio.ByteBuffer;
import lombok.Value;

@Value
public class ByteBufferPayload implements SizeAware {
  ByteBuffer value;

  @Override
  public long getSize() {
    return value.remaining();
  }
}
