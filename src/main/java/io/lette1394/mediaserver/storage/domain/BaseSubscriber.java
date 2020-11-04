package io.lette1394.mediaserver.storage.domain;


import lombok.RequiredArgsConstructor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@RequiredArgsConstructor
public class BaseSubscriber<T extends Payload> implements Subscriber<T> {
  protected final Subscriber<? super T> delegate;

  @Override
  public void onSubscribe(Subscription s) {
    delegate.onSubscribe(s);
  }

  @Override
  public void onNext(T t) {
    delegate.onNext(t);
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
