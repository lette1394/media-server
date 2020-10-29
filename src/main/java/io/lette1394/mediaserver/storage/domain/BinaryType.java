package io.lette1394.mediaserver.storage.domain;

public enum BinaryType {
  CHUNKED,
  LENGTH_AWARE;

  public boolean is(BinaryType other) {
    return this == other;
  }
}
