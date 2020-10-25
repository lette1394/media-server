package io.lette1394.mediaserver.storage.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Snapshot {
  private final Identifier identifier;
  private final long size;

  private State state;
  private ObjectLifeCycle lifeCycle;
  private long progressingSize;

  @Builder(access = AccessLevel.PRIVATE)
  private Snapshot(Identifier identifier,
    State state, ObjectLifeCycle lifeCycle, long size, long progressingSize) {
    this.identifier = identifier;
    this.state = state;
    this.lifeCycle = lifeCycle;
    this.size = size;
    this.progressingSize = progressingSize;
  }

  static Snapshot initial(Object object) {
    return Snapshot.builder()
      .identifier(object.identifier)
      .size(object.getSize())
      .lifeCycle(ObjectLifeCycle.NO_OPERATION)
      .state(object.getObjectState())
      .progressingSize(0)
      .build();
  }

  Snapshot update(ObjectLifeCycle lifeCycle) {
    this.lifeCycle = lifeCycle;
    return this;
  }

  Snapshot update(State state) {
    this.state = state;
    return this;
  }

  // 다른 패키지로 옮기는 걸 고려
  Snapshot update(long progressingSize) {
    this.progressingSize = progressingSize;
    return this;
  }
}
