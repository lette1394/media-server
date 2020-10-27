package io.lette1394.mediaserver.storage.domain.object;

import io.lette1394.mediaserver.common.PositiveLong;
import io.lette1394.mediaserver.common.TimeStamp;
import lombok.Builder;

public class FulfilledObject extends Object {

  private final PositiveLong size;

  @Builder
  public FulfilledObject(Identifier identifier,
    Policy policy, TimeStamp timeStamp, Tags tags,
    PositiveLong size) {
    super(identifier, policy, timeStamp, tags);
    this.size = size;
  }

  @Override
  public long getSize() {
    return size.get();
  }

  @Override
  public Type getType() {
    return Type.FULFILLED;
  }
}
