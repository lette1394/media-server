package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.Tries;
import io.vavr.control.Try;
import lombok.Value;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Value
class ControllableBinarySupplier<BUFFER extends Payload> implements BinarySupplier<BUFFER> {

  BinarySupplier<BUFFER> binarySupplier;
  Policy policy;

  @Override
  public Publisher<BUFFER> getAsync() {
    final Publisher<BUFFER> async = binarySupplier.getAsync();
    return subscriber -> async.subscribe(new Subscriber<>() {
      private long acc = 0L;

      @Override
      public void onSubscribe(Subscription s) {
        checkSucceed(policy.beforeTransfer());

        subscriber.onSubscribe(s);
      }

      @Override
      public void onNext(BUFFER item) {
        final long size = item.getSize();
        if (size > 0) {
          acc += size;
          checkSucceed(policy.duringTransferring(acc));
        }
        subscriber.onNext(item);
      }

      @Override
      public void onError(Throwable t) {
        subscriber.onError(t);
      }

      @Override
      public void onComplete() {
        checkSucceed(policy.afterTransferred(acc));
        subscriber.onComplete();
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
