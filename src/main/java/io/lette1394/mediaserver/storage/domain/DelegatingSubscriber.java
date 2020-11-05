package io.lette1394.mediaserver.storage.domain;

import lombok.RequiredArgsConstructor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@RequiredArgsConstructor
public class DelegatingSubscriber<T> implements Subscriber<T> {
  private final Subscriber<? super T> subscriber;

  @Override
  public void onSubscribe(Subscription s) {
    subscriber.onSubscribe(s);
  }

  @Override
  public void onNext(T t) {
    subscriber.onNext(t);
  }

  @Override
  public void onError(Throwable t) {
    subscriber.onError(t);
  }

  @Override
  public void onComplete() {
    subscriber.onComplete();
  }
}
