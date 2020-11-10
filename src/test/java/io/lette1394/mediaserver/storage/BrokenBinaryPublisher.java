package io.lette1394.mediaserver.storage;

import static io.lette1394.mediaserver.common.Contracts.require;
import static java.lang.String.format;
import static java.util.Objects.nonNull;

import io.lette1394.mediaserver.storage.domain.BinaryPublisher;
import io.lette1394.mediaserver.storage.domain.DelegatingBinaryPublisher;
import io.lette1394.mediaserver.storage.domain.Payload;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class BrokenBinaryPublisher<T extends Payload> extends DelegatingBinaryPublisher<T> {
  long exceptionAt;
  long totalLength;

  public BrokenBinaryPublisher(long exceptionAt, BinaryPublisher<T> delegate) {
    super(delegate);
    require(nonNull(delegate), "nonNull(binarySupplier)");
    require(exceptionAt >= 0, "exceptionAt >= 0");
    require(delegate.length().isPresent()
      && (delegate.length().get() > exceptionAt), "delegate.length() > exceptionAt");

    this.totalLength = delegate.length().get();
    this.exceptionAt = exceptionAt;
  }

  @Override
  public Publisher<T> publisher() {
    final Publisher<T> async = delegate.publisher();
    return new Publisher<>() {
      private long position = 0;
      private boolean triggered = false;

      @Override
      public void subscribe(Subscriber<? super T> subscriber) {
        async.subscribe(new Subscriber<>() {
          @Override
          public void onSubscribe(Subscription s) {
            subscriber.onSubscribe(s);
          }

          @Override
          public void onNext(T item) {
            if (triggered) {
              return;
            }
            if (position >= exceptionAt) {
              triggered = true;
              onError(new BrokenIOException(
                format("broken read triggered, size:[%s], exceptionAt:[%s]",
                  totalLength,
                  exceptionAt)));
            } else {
              position += item.getSize();
              subscriber.onNext(item);
            }
          }

          @Override
          public void onError(Throwable t) {
            subscriber.onError(t);
          }

          @Override
          public void onComplete() {
            if (triggered) {
              return;
            }
            subscriber.onComplete();
          }
        });
      }
    };
  }
}
