package io.lette1394.mediaserver.common;

import lombok.Value;

@Value
public class PositiveLong {
  long value;

  public PositiveLong(long value) {
    Contracts.require(value > 0, "long value requires more than 0");
    this.value = value;
  }

  public static PositiveLong positiveLong(long value) {
    return new PositiveLong(value);
  }
}
