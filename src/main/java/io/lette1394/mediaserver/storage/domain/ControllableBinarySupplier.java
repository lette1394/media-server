package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.Tries;
import io.vavr.control.Try;
import lombok.Value;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Value
class ControllableBinarySupplier<T extends SizeAware> implements BinarySupplier<T> {

  BinarySupplier<T> binarySupplier;
  Policy policy;

  @Override
  public Publisher<T> getAsync() {
    final Publisher<T> async = binarySupplier.getAsync();
    return subscriber -> async.subscribe(new Subscriber<>() {
      private long acc = 0L;

      @Override
      public void onSubscribe(Subscription s) {
        checkSucceed(policy.beforeTransfer());

        subscriber.onSubscribe(s);
      }

      @Override
      public void onNext(T item) {
        final long remaining = item.getSize();
        subscriber.onNext(item);

        if (remaining > 0) {
          acc += remaining;
          checkSucceed(policy.duringTransferring(acc));
        }
      }

      @Override
      public void onError(Throwable t) {
        subscriber.onError(t);
      }

      @Override
      public void onComplete() {
        subscriber.onComplete();
        checkSucceed(policy.afterTransferred(acc));
      }

      private void checkSucceed(Try<?> result) {
        if (result.isSuccess()) {
          return;
        }
        onError(result.getCause());
      }
    });
  }

  public interface Policy {

    default Try<Void> beforeTransfer() {
      return Tries.SUCCESS;
    }

    default Try<Void> duringTransferring(long currentLength) {
      return Tries.SUCCESS;
    }

    default Try<Void> afterTransferred(long totalLength) {
      return Tries.SUCCESS;
    }
  }
}
