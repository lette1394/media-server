package io.lette1394.mediaserver.storage.domain.object;

public enum Type {
  INITIAL,
  PENDING,
  FULFILLED;

  public boolean is(Type type) {
    return this == type;
  }
}
