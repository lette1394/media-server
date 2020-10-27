package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.storage.domain.binary.Type;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Delegate;

@Getter
class BinarySnapshot {
  @Delegate(excludes = Enum.class)
  private Type type;
  private long progressingSize;
  @Delegate(excludes = Enum.class)
  private LifeCycle lifeCycle;

  @Builder
  public BinarySnapshot(Type type, long progressingSize) {
    this.type = type;
    this.progressingSize = progressingSize;
  }

  boolean isOver(long size) {
    return size > this.progressingSize;
  }

  BinarySnapshot update(long progressingSize) {
    this.progressingSize = progressingSize;
    return this;
  }

  BinarySnapshot update(Type type) {
    this.type = type;
    return this;
  }

  BinarySnapshot update(LifeCycle lifeCycle) {
    this.lifeCycle = lifeCycle;
    return this;
  }
}
