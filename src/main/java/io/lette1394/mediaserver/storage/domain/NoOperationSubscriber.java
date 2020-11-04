package io.lette1394.mediaserver.storage.domain;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class NoOperationSubscriber<T extends Payload> implements Subscriber<T> {
  private static final NoOperationSubscriber<?> INSTANCE = new NoOperationSubscriber<>();

  private NoOperationSubscriber() {
  }

  @SuppressWarnings("unchecked")
  public static <T extends Payload> NoOperationSubscriber<T> instance() {
    return (NoOperationSubscriber<T>) INSTANCE;
  }

  @Override
  public void onSubscribe(Subscription s) {

  }

  @Override
  public void onNext(T t) {

  }

  @Override
  public void onError(Throwable t) {

  }

  @Override
  public void onComplete() {

  }
}
