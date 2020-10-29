package io.lette1394.mediaserver.storage.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Delegate;

@Getter
public class ObjectSnapshot {

  private final Identifier identifier;
  private final long size;
  @Delegate(excludes = Enum.class)
  private final ObjectType objectType;
  @Delegate(excludes = Enum.class)
  private final Command command;

  @Builder
  public ObjectSnapshot(Identifier identifier, long size,
    ObjectType objectType, Command command) {
    this.identifier = identifier;
    this.size = size;
    this.objectType = objectType;
    this.command = command;
  }
}
