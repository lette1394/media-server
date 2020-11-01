package io.lette1394.mediaserver.storage.infrastructure;

import io.lette1394.mediaserver.storage.domain.Payload;
import lombok.Value;

@Value
public class BytePayload implements Payload {
  byte value;

  public BytePayload(int value) {
    this.value = (byte) value;
  }

  @Override
  public long getSize() {
    return 1;
  }

  @Override
  public void release() {
    // no op
  }
}
