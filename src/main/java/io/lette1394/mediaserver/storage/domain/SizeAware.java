package io.lette1394.mediaserver.storage.domain;

public interface SizeAware {

  long getSize();

  default void release() {
  }
}
