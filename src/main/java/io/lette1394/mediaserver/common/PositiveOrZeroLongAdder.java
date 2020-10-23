package io.lette1394.mediaserver.common;

import java.util.concurrent.atomic.LongAdder;
import lombok.Value;

@Value(staticConstructor = "positiveOrZeroLongAdder")
public class PositiveOrZeroLongAdder {
  LongAdder longAdder;

  public PositiveOrZeroLongAdder(long initialValue) {
    Contracts.require(initialValue >= 0, "PositiveOrZeroLongAdder needs positive or zero value");
    final LongAdder longAdder = new LongAdder();
    longAdder.add(initialValue);

    this.longAdder = longAdder;
  }

  public PositiveOrZeroLongAdder() {
    this(0L);
  }

  public long get() {
    return longAdder.longValue();
  }

  public void add(long value) {
    longAdder.add(value);
  }
}
