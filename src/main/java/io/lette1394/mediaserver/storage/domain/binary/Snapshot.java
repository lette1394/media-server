package io.lette1394.mediaserver.storage.domain.binary;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Delegate;

@Value
public class Snapshot {
  @Delegate(excludes = Enum.class)
  Type type;
  long size;

  @Builder
  public Snapshot(Type type, long size) {
    this.type = type;
    this.size = size;
  }

  boolean isOver(long size) {
    return size > this.size;
  }
}
