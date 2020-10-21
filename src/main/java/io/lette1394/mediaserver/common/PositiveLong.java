package io.lette1394.mediaserver.common;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
public class PositiveLong {
  long value;

  public static PositiveLong positiveLong(long value) {
    return new PositiveLong(value);
  }
}
