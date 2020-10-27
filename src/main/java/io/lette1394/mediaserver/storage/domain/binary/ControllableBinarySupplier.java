package io.lette1394.mediaserver.storage.domain.binary;

import io.lette1394.mediaserver.common.Tries;
import io.vavr.control.Try;
import java.nio.ByteBuffer;
import lombok.Value;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Value
public class ControllableBinarySupplier implements BinarySupplier {

  BinarySupplier binarySupplier;
  Policy policy;

  @Override
  public Publisher<ByteBuffer> getAsync() {
    final Publisher<ByteBuffer> async = binarySupplier.getAsync();
    return subscriber -> async.subscribe(new Subscriber<>() {
      private long acc = 0L;

      @Override
      public void onSubscribe(Subscription s) {
        checkSucceed(policy.beforeTransfer());

        subscriber.onSubscribe(s);
      }

      @Override
      public void onNext(ByteBuffer byteBuffer) {
        final int remaining = byteBuffer.remaining();
        subscriber.onNext(byteBuffer);

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
      return Tries.SUCCEED;
    }

    default Try<Void> duringTransferring(long currentLength) {
      return Tries.SUCCEED;
    }

    default Try<Void> afterTransferred(long totalLength) {
      return Tries.SUCCEED;
    }
  }
}
