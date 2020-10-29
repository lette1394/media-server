package io.lette1394.mediaserver.storage.domain;

import org.reactivestreams.Publisher;

class BaseLengthAwareBinarySupplier<BUFFER extends SizeAware> implements LengthAwareBinarySupplier<BUFFER> {

  @Override
  public long getLength() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Publisher<BUFFER> getAsync() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }
}
