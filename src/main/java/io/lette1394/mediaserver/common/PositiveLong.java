package io.lette1394.mediaserver.common;

public class PositiveLong {
  private final long value;

  public PositiveLong(long value) {
    Contracts.require(value > 0, "long value requires more than 0");
    this.value = value;
  }

  public static PositiveLong positiveLong(long value) {
    return new PositiveLong(value);
  }

  public long get() {
    return value;
  }
}
