package io.lette1394.mediaserver.storage.domain;

public class PendingObjectSnapshot extends ObjectSnapshot {
  public PendingObjectSnapshot(long size,
    ObjectType objectType, Command command) {
    super(size, objectType, command);
  }
}
