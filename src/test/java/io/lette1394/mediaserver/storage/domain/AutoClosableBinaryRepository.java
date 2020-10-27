package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.storage.domain.binary.BinaryRepository;

public interface AutoClosableBinaryRepository
  extends BinaryRepository, AutoCloseable {

}
