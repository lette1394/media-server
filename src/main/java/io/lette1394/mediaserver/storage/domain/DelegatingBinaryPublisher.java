package io.lette1394.mediaserver.storage.domain;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;

@RequiredArgsConstructor
public class DelegatingBinaryPublisher<BUFFER extends Payload> implements BinaryPublisher<BUFFER> {
  protected final BinaryPublisher<BUFFER> delegate;

  @Override
  public Publisher<BUFFER> publisher() {
    return delegate.publisher();
  }

  @Override
  public Context currentContext() {
    return delegate.currentContext();
  }

  @Override
  public Optional<Long> length() {
    return delegate.length();
  }
}
