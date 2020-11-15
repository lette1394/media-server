package io.lette1394.mediaserver.storage.infrastructure;

import io.lette1394.mediaserver.storage.domain.Payload;
import lombok.Value;

@Value
public class StringPayload implements Payload {
  String value;

  @Override
  public long getSize() {
    return value.length();
  }
}
