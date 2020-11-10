package io.lette1394.mediaserver.storage.domain;

@FunctionalInterface
public interface Payload {

  long getSize();

  default void release() {
    // no op
  }
}
