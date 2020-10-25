package io.lette1394.mediaserver.storage.domain.binary;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import lombok.Value;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Value
public class ListenableBinarySupplier implements BinarySupplier {
  BinarySupplier binarySupplier;
  Listener listener;

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
      private boolean isFirstRead = true;
      private long accumulate = 0L;

      @Override
      public int read() throws IOException {
        try {
          notifyFirst();
          final int read = sync.read();
          if (read != -1) {
            notifyMiddle();
          }
          if (read == -1) {
            notifyLast();
          }
          return read;
        } catch (Exception e) {
          notifyAborted(e);
          throw e;
        }
      }

      private void notifyFirst() {
        if (isFirstRead) {
          listener.beforeTransfer();
          isFirstRead = false;
        }
      }

      private void notifyMiddle() {
        accumulate += 1;
        listener.duringTransferring(accumulate, getLength());
      }

      private void notifyLast() {
        listener.afterTransferred(getLength());
      }

      private void notifyAborted(Throwable throwable) {
        listener.transferAborted(throwable);
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
        subscriber.onSubscribe(s);
        listener.beforeTransfer();
      }

      @Override
      public void onNext(ByteBuffer byteBuffer) {
        final int remaining = byteBuffer.remaining();
        subscriber.onNext(byteBuffer);

        if (remaining > 0) {
          accumulate += remaining;
          listener.duringTransferring(accumulate, getLength());
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
        listener.afterTransferred(accumulate);
      }
    });
  }

  public interface Listener {
    default void beforeTransfer() {
    }

    default void duringTransferring(long currentLength, long totalLength) {
    }

    default void afterTransferred(long totalLength) {
    }

    // 한 번만 실행된다는 걸 보장 해야 한다.
    default void transferAborted(Throwable throwable) {
    }
  }
}
