package io.lette1394.mediaserver.storage.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Delegate;

@Getter
class BinarySnapshot {

  private long progressingSize;
  @Delegate(excludes = Enum.class)
  private BinaryLifecycle binaryLifecycle;

  @Builder
  BinarySnapshot(long progressingSize,
    BinaryLifecycle binaryLifecycle) {
    this.progressingSize = progressingSize;
    this.binaryLifecycle = binaryLifecycle;
  }

  static BinarySnapshot initial() {
    return BinarySnapshot.builder()
      .progressingSize(0)
      .binaryLifecycle(BinaryLifecycle.NO_OPERATION)
      .build();
  }

  boolean isOver(long progressingSize) {
    return progressingSize > this.progressingSize;
  }

  BinarySnapshot update(long progressingSize) {
    this.progressingSize = progressingSize;
    return this;
  }

  BinarySnapshot update(BinaryLifecycle binaryLifecycle) {
    this.binaryLifecycle = binaryLifecycle;
    return this;
  }
}
