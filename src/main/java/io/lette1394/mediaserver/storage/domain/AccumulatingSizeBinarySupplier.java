package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.PositiveOrZeroLongAdder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import lombok.Value;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Value
public class AccumulatingSizeBinarySupplier implements BinarySupplier {
  BinarySupplier delegate;
  Listener listener;
  PositiveOrZeroLongAdder currentSize = new PositiveOrZeroLongAdder();

  public long currentSize() {
    return currentSize.get();
  }

  @Override
  public boolean isSyncSupported() {
    return delegate.isSyncSupported();
  }

  @Override
  public boolean isAsyncSupported() {
    return delegate.isAsyncSupported();
  }

  @Override
  public long getLength() {
    return delegate.getLength();
  }

  @Override
  public InputStream getSync() {
    final InputStream sync = delegate.getSync();
    return new InputStream() {
      @Override
      public int read() throws IOException {
        final int read = sync.read();
        if (read != -1) {
          currentSize.add(1L);
          notifyChanges();
        }
        return read;
      }
    };
  }

  @Override
  public Publisher<ByteBuffer> getAsync() {
    final Publisher<ByteBuffer> async = delegate.getAsync();
    return subscriber -> async.subscribe(new Subscriber<>() {
      @Override
      public void onSubscribe(Subscription s) {
        subscriber.onSubscribe(s);
      }

      @Override
      public void onNext(ByteBuffer byteBuffer) {
        if (byteBuffer.hasRemaining()) {
          currentSize.add(byteBuffer.remaining());
          notifyChanges();
        }
        subscriber.onNext(byteBuffer);
      }

      @Override
      public void onError(Throwable t) {
        subscriber.onError(t);
      }

      @Override
      public void onComplete() {
        subscriber.onComplete();
      }
    });
  }

  @FunctionalInterface
  interface Listener {
    void sizeChanged(long value);
  }

  private void notifyChanges() {
    listener.sizeChanged(currentSize.get());
  }
}
