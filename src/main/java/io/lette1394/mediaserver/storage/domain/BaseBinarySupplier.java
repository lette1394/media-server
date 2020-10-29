package io.lette1394.mediaserver.storage.domain;

import org.reactivestreams.Publisher;

class BaseBinarySupplier<BUFFER extends SizeAware> implements BinarySupplier<BUFFER> {

  @Override
  public Publisher<BUFFER> getAsync() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }
}
