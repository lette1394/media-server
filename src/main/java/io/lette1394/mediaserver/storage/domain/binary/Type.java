package io.lette1394.mediaserver.storage.domain.binary;

public enum Type {
  CHUNKED,
  LENGTH_AWARE;

  public boolean is(Type other) {
    return this == other;
  }
}
