package io.lette1394.mediaserver.domain.storage.object;

import lombok.Builder;

@Builder
public class ObjectSnapshot {
  public final Identifier identifier;
  public final ObjectState state;
  public final ObjectLifeCycle lifeCycle;
  public final long size;
}
