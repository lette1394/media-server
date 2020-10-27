package io.lette1394.mediaserver.storage.domain;

import lombok.Value;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Value
class ListenableBinarySupplier<T extends SizeAware> implements BinarySupplier<T> {

  BinarySupplier<T> binarySupplier;
  ListenableBinarySupplier.Listener listener;

  @Override
  public Publisher<T> getAsync() {
    final Publisher<T> async = binarySupplier.getAsync();
    return subscriber -> async.subscribe(new Subscriber<>() {
      private long acc = 0L;

      @Override
      public void onSubscribe(Subscription s) {
        subscriber.onSubscribe(s);
        listener.beforeTransfer();
      }

      @Override
      public void onNext(T item) {
        final long remaining = item.getSize();
        subscriber.onNext(item);

        if (remaining > 0) {
          acc += remaining;
          listener.duringTransferring(acc);
        }
      }

      @Override
      public void onError(Throwable t) {
        subscriber.onError(t);
        listener.transferAborted(t);
      }

      @Override
      public void onComplete() {
        subscriber.onComplete();
        listener.afterTransferred(acc);
      }
    });
  }

  interface Listener {

    default void beforeTransfer() {
    }

    default void duringTransferring(long currentLength) {
    }

    // TODO: auto close resource
    default void afterTransferred(long totalLength) {
    }

    // TODO: auto close resource
    //  AND
    //  한 번만 실행된다는 걸 보장 해야 한다.
    default void transferAborted(Throwable throwable) {
    }
  }
}
