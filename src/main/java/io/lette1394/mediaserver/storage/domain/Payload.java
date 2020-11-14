package io.lette1394.mediaserver.storage.domain;

@FunctionalInterface
public interface Payload {

  long getSize();

  default Payload release() {
    return this;
  }

  default Payload retain() {
    return this;
  }
}
