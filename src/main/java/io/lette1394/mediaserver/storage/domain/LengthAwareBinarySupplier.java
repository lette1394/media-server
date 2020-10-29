package io.lette1394.mediaserver.storage.domain;

public interface LengthAwareBinarySupplier<BUFFER extends SizeAware> extends BinarySupplier<BUFFER> {

  long getLength();
}
