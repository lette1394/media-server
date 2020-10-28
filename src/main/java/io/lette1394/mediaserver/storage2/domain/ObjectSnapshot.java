package io.lette1394.mediaserver.storage2.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Delegate;

@Getter
public class ObjectSnapshot {

  private final long size;
  @Delegate(excludes = Enum.class)
  private final ObjectType objectType;
  @Delegate(excludes = Enum.class)
  private final Command command;

  public static ObjectSnapshot initial() {
    return ObjectSnapshot.builder()
      .command(Command.NO_OP)
      .size(0)
      .build();
  }

  @Builder
  public ObjectSnapshot(long size, ObjectType objectType, Command command) {
    this.size = size;
    this.objectType = objectType;
    this.command = command;
  }
}
