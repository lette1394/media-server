package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.Result;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class ControllableBinarySupplier extends DelegatingBinarySupplier {
  private final Policy policy;

  public ControllableBinarySupplier(BinarySupplier binarySupplier, Policy policy) {
    super(binarySupplier);
    this.policy = policy;
  }

  @Override
  public InputStream getSync() {
    final InputStream sync = super.getSync();
    return new InputStream() {
      boolean isFirstRead = true;
      private long accumulate = 0L;

      @Override
      public int read() throws IOException {
        checkFirst();

        final int read = sync.read();
        if (read != -1) {
          checkMiddle();
        }
        if (read == -1) {
          checkLast();
        }

        return read;
      }

      private void checkFirst() {
        if (!isFirstRead) {
          return;
        }
        isFirstRead = false;

        checkSucceed(policy.beforeTransfer());
      }

      private void checkMiddle() {
        accumulate += 1;
        checkSucceed(policy.duringTransferring(accumulate, getSize()));
      }

      private void checkLast() {
        checkSucceed(policy.afterTransferred(getSize()));
      }

      private void checkSucceed(Result<?> result) {
        if (result.isSucceed()) {
          return;
        }
        throw new RuntimeException(result.getThrowable());
      }
    };
  }

  @Override
  public Publisher<ByteBuffer> getAsync() {
    final Publisher<ByteBuffer> async = super.getAsync();
    return subscriber -> async.subscribe(new Subscriber<>() {
      private long accumulate = 0L;

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
          accumulate += remaining;
          checkSucceed(policy.duringTransferring(accumulate, getSize()));
        }
      }

      @Override
      public void onError(Throwable t) {
        subscriber.onError(t);
      }

      @Override
      public void onComplete() {
        subscriber.onComplete();
        checkSucceed(policy.afterTransferred(accumulate));
      }

      private void checkSucceed(Result<?> result) {
        if (result.isSucceed()) {
          return;
        }
        onError(result.getThrowable());
      }
    });
  }

  interface Policy {
    default Result<Void> beforeTransfer() {
      return Result.succeed();
    }

    default Result<Void> duringTransferring(long currentSize, long total) {
      return Result.succeed();
    }

    default Result<Void> afterTransferred(long totalLength) {
      return Result.succeed();
    }
  }
}
