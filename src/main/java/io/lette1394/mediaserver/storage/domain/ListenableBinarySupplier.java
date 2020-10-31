package io.lette1394.mediaserver.storage.domain;

import lombok.Value;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Value
class ListenableBinarySupplier<BUFFER extends Payload> implements BinarySupplier<BUFFER> {

  BinarySupplier<BUFFER> binarySupplier;
  Listener listener;

  @Override
  public Publisher<BUFFER> getAsync() {
    final Publisher<BUFFER> async = binarySupplier.getAsync();
    return subscriber -> async.subscribe(new Subscriber<>() {
      private long acc = 0L;

      @Override
      public void onSubscribe(Subscription s) {
        listener.beforeTransfer();
        subscriber.onSubscribe(s);
      }

      @Override
      public void onNext(BUFFER item) {
        final long size = item.getSize();
        if (size > 0) {
          acc += size;
          listener.duringTransferring(acc);
        }
        subscriber.onNext(item);
      }

      @Override
      public void onError(Throwable t) {
        listener.transferAborted(t);
        subscriber.onError(t);
      }

      @Override
      public void onComplete() {
        listener.afterTransferred(acc);
        subscriber.onComplete();
      }
    });
  }

  public interface Listener {

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
