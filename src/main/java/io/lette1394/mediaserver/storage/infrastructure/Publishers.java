package io.lette1394.mediaserver.storage.infrastructure;

import io.lette1394.mediaserver.storage.domain.SizeAware;
import java.util.function.Function;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class Publishers {

  public static <T, R> Publisher<R> convert(
    Publisher<T> publisher, Function<T, R> mapper) {
    return subscriber -> publisher.subscribe(new Subscriber<T>() {
      @Override
      public void onSubscribe(Subscription s) {
        subscriber.onSubscribe(s);
      }

      @Override
      public void onNext(T t) {
        subscriber.onNext(mapper.apply(t));
      }

      @Override
      public void onError(Throwable t) {
        subscriber.onError(t);
      }

      @Override
      public void onComplete() {
        subscriber.onComplete();
      }
    });
  }
}
