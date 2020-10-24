package io.lette1394.mediaserver.storage.domain;

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
  public long getSize() {
    return binarySupplier.getSize();
  }

  @Override
  public InputStream getSync() {
    final InputStream sync = binarySupplier.getSync();
    return new InputStream() {
      private boolean isFirstRead = true;
      private long accumulate = 0L;

      @Override
      public int read() throws IOException {
        notifyFirst();

        final int read = sync.read();
        if (read != -1) {
          notifyMiddle();
        }
        if (read == -1) {
          notifyLast();
        }

        return read;
      }

      private void notifyFirst() {
        if (isFirstRead) {
          listener.beforeTransfer();
          isFirstRead = false;
        }
      }

      private void notifyMiddle() {
        accumulate += 1;
        listener.duringTransferring(accumulate, getSize());
      }

      private void notifyLast() {
        listener.afterTransferred(getSize());
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
        listener.beforeTransfer();
      }

      @Override
      public void onNext(ByteBuffer byteBuffer) {
        final int remaining = byteBuffer.remaining();
        subscriber.onNext(byteBuffer);

        if (remaining > 0) {
          accumulate += remaining;
          listener.duringTransferring(accumulate, getSize());
        }
      }

      @Override
      public void onError(Throwable t) {
        subscriber.onError(t);
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

    default void duringTransferring(long currentSize, long total) {
    }

    default void afterTransferred(long totalLength) {
    }

    default void transferAborted(Throwable throwable) {
    }
  }
}
