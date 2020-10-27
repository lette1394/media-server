package io.lette1394.mediaserver.storage.domain.object;

import io.lette1394.mediaserver.common.PositiveLong;
import io.lette1394.mediaserver.common.TimeStamp;
import lombok.Builder;

public class PendingObject extends Object {

  private final PositiveLong size;

  @Builder
  public PendingObject(Identifier identifier,
    Policy policy, TimeStamp timeStamp, Tags tags, PositiveLong size) {
    super(identifier, policy, timeStamp, tags);
    this.size = size;
  }

  @Override
  public Type getType() {
    return Type.PENDING;
  }

  @Override
  public long getSize() {
    return size.get();
  }
}
