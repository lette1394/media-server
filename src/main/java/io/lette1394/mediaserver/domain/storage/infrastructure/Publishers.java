package io.lette1394.mediaserver.domain.storage.infrastructure;

public class Publishers {
  public static <T> org.reactivestreams.Publisher<T> adapt(java.util.concurrent.Flow.Publisher<T> publisher) {
    return new org.reactivestreams.Publisher<T>() {
      @Override
      public void subscribe(org.reactivestreams.Subscriber<? super T> s) {
        publisher.subscribe(new java.util.concurrent.Flow.Subscriber<T>() {
          @Override
          public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) {
            s.onSubscribe(new org.reactivestreams.Subscription() {
              @Override
              public void request(long n) {
                subscription.request(n);
              }

              @Override
              public void cancel() {
                subscription.cancel();
              }
            });
          }

          @Override
          public void onNext(T item) {
            s.onNext(item);
          }

          @Override
          public void onError(Throwable throwable) {
            s.onError(throwable);
          }

          @Override
          public void onComplete() {
            s.onComplete();
          }
        });
      }
    };
  }
}
