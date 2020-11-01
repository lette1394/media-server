package io.lette1394.mediaserver.storage.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Delegate;

@Getter
public class ObjectSnapshot {

  private long size;
  @Delegate(excludes = Enum.class)
  private ObjectType objectType;
  @Delegate(excludes = Enum.class)
  private Command command;

  public static ObjectSnapshot initial(long size) {
    return ObjectSnapshot.builder()
      .size(size)
      .objectType(ObjectType.INITIAL)
      .command(Command.UPLOAD)
      .build();
  }

  @Builder
  public ObjectSnapshot(
    long size,
    ObjectType objectType,
    Command command) {
    this.size = size;
    this.objectType = objectType;
    this.command = command;
  }

  // Object inner class로 변경하는 것을 검토 (ObjectPolicy에서 update를 수행할 수 있다)
  ObjectSnapshot update(long size) {
    this.size = size;
    return this;
  }

  ObjectSnapshot update(ObjectType objectType) {
    this.objectType = objectType;
    return this;
  }

  ObjectSnapshot update(Command command) {
    this.command = command;
    return this;
  }
}
