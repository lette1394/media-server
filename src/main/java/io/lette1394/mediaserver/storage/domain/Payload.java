package io.lette1394.mediaserver.storage.domain;

@FunctionalInterface
public interface Payload {

  long getSize();

  default void release() {
    // no op
  }

  default void retain() {
    // no op
  }

  default void retain(int count) {
    // no op
  }
}
