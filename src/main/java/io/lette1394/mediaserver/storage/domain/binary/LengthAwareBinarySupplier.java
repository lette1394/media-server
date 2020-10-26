package io.lette1394.mediaserver.storage.domain.binary;

public interface LengthAwareBinarySupplier extends BinarySupplier {

  long getLength();
}
