package io.lette1394.mediaserver.storage.domain;

import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;

@RequiredArgsConstructor
public class DelegatingBinarySupplier<BUFFER extends Payload> implements BinarySupplier<BUFFER> {
  protected final BinarySupplier<BUFFER> delegate;

  @Override
  public Publisher<BUFFER> publisher() {
    return delegate.publisher();
  }

  @Override
  public Context currentContext() {
    return delegate.currentContext();
  }
}
