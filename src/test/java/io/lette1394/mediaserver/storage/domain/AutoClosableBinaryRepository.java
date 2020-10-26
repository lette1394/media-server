package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.storage.domain.binary.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.binary.BinarySupplier;

public interface AutoClosableBinaryRepository<T extends BinarySupplier>
  extends BinaryRepository<T>, AutoCloseable {
}
