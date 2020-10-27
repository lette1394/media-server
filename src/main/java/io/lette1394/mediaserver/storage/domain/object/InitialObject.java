package io.lette1394.mediaserver.storage.domain.object;

import io.lette1394.mediaserver.common.TimeStamp;
import lombok.Builder;

public class InitialObject extends Object {

  @Builder
  public InitialObject(Identifier identifier,
    Policy policy, TimeStamp timeStamp, Tags tags) {
    super(identifier, policy, timeStamp, tags);
  }

  @Override
  public Type getType() {
    return Type.INITIAL;
  }

  @Override
  public long getSize() {
    return 0L;
  }
}
