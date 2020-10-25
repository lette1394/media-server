package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.Tries;
import io.vavr.control.Try;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import lombok.Value;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Value
class ControllableBinarySupplier implements BinarySupplier {
  BinarySupplier binarySupplier;
  Policy policy;

  @Override
  public boolean isSyncSupported() {
    return binarySupplier.isSyncSupported();
  }

  @Override
  public boolean isAsyncSupported() {
    return binarySupplier.isAsyncSupported();
  }

  @Override
  public long getLength() {
    return binarySupplier.getLength();
  }

  @Override
  public InputStream getSync() {
    final InputStream sync = binarySupplier.getSync();
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
        checkSucceed(policy.duringTransferring(accumulate, getLength()));
      }

      private void checkLast() {
        checkSucceed(policy.afterTransferred(getLength()));
      }

      private void checkSucceed(Try<?> result) {
        if (result.isSuccess()) {
          return;
        }
        throw new RuntimeException(result.getCause());
      }
    };
  }

  @Override
  public Publisher<ByteBuffer> getAsync() {
    final Publisher<ByteBuffer> async = binarySupplier.getAsync();
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
          checkSucceed(policy.duringTransferring(accumulate, getLength()));
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

      private void checkSucceed(Try<?> result) {
        if (result.isSuccess()) {
          return;
        }
        onError(result.getCause());
      }
    });
  }

  interface Policy {
    default Try<Void> beforeTransfer() {
      return Tries.SUCCEED;
    }

    default Try<Void> duringTransferring(long currentSize, long total) {
      return Tries.SUCCEED;
    }

    default Try<Void> afterTransferred(long totalLength) {
      return Tries.SUCCEED;
    }
  }
}
