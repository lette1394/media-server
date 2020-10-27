package io.lette1394.mediaserver.storage.domain.object;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Delegate;

@Getter
public class Snapshot {

  private final Identifier identifier;
  private final long size;
  @Delegate(excludes = Enum.class)
  private final Type type;
  @Delegate(excludes = Enum.class)
  private final Command command;

  @Builder
  public Snapshot(Identifier identifier, long size,
    Type type, Command command) {
    this.identifier = identifier;
    this.size = size;
    this.type = type;
    this.command = command;
  }
}
