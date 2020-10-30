package io.lette1394.mediaserver.storage.infrastructure;

import io.lette1394.mediaserver.storage.domain.SizeAware;
import lombok.Value;

@Value
public class StringPayload implements SizeAware {
  String value;

  @Override
  public long getSize() {
    return value.length();
  }
}
