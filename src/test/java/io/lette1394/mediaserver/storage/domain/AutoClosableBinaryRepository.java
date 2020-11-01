package io.lette1394.mediaserver.storage.domain;

public interface AutoClosableBinaryRepository<T extends Payload>
  extends BinaryRepository<T>, AutoCloseable {

}
