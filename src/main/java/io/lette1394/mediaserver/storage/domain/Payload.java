package io.lette1394.mediaserver.storage.domain;

public interface Payload {

  long getSize();

  void release();
}
