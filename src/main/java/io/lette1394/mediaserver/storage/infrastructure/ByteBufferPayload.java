package io.lette1394.mediaserver.storage.infrastructure;

import io.lette1394.mediaserver.storage.domain.Payload;
import java.nio.ByteBuffer;
import lombok.Value;

@Value
public class ByteBufferPayload implements Payload {
  ByteBuffer value;

  @Override
  public long getSize() {
    return value.remaining();
  }

  @Override
  public void release() {
    
  }
}
