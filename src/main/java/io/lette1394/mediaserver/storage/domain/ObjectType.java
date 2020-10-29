package io.lette1394.mediaserver.storage.domain;

public enum ObjectType {
  INITIAL,
  PENDING,
  FULFILLED;

  public boolean is(ObjectType objectType) {
    return this == objectType;
  }
}
