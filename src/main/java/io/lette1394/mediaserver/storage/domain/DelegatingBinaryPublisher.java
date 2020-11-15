package io.lette1394.mediaserver.storage.domain;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Subscriber;

@RequiredArgsConstructor
public class DelegatingBinaryPublisher<P extends Payload> implements BinaryPublisher<P> {
  protected final BinaryPublisher<P> delegate;

  @Override
  public void subscribe(Subscriber<? super P> s) {
    delegate.subscribe(s);
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
