package io.lette1394.mediaserver.storage.domain;

import lombok.RequiredArgsConstructor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@RequiredArgsConstructor
public abstract class DelegatingSubscriber<T, R> implements Subscriber<T> {
  protected final Subscriber<? super R> delegate;

  @Override
  public void onSubscribe(Subscription s) {
    delegate.onSubscribe(s);
  }

  @Override
  public void onError(Throwable t) {
    delegate.onError(t);
  }

  @Override
  public void onComplete() {
    delegate.onComplete();
  }
}
