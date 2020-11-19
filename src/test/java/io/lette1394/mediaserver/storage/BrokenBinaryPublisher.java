package io.lette1394.mediaserver.storage;

import static io.lette1394.mediaserver.common.Contracts.require;
import static java.lang.String.format;
import static java.util.Objects.nonNull;

import io.lette1394.mediaserver.storage.domain.BinaryPublisher;
import io.lette1394.mediaserver.storage.domain.DelegatingBinaryPublisher;
import io.lette1394.mediaserver.storage.domain.Payload;
import java.io.IOException;
import lombok.Getter;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class BrokenBinaryPublisher<P extends Payload> extends DelegatingBinaryPublisher<P> {
  private final long exceptionAt;
  private final long totalLength;

  public BrokenBinaryPublisher(long exceptionAt, BinaryPublisher<P> delegate) {
    super(delegate);
    require(nonNull(delegate), "nonNull(binarySupplier)");
    require(exceptionAt >= 0, "exceptionAt >= 0");
    require(delegate.length().isPresent()
      && (delegate.length().get() > exceptionAt), "delegate.length() > exceptionAt");

    this.totalLength = delegate.length().get();
    this.exceptionAt = exceptionAt;
  }

  @Override
  public void subscribe(Subscriber<? super P> subscriber) {
    delegate.subscribe(new Subscriber<>() {
      long position = 0;
      boolean triggered = false;

      @Override
      public void onSubscribe(Subscription s) {
        subscriber.onSubscribe(s);
      }

      @Override
      public void onNext(P item) {
        if (triggered) {
          return;
        }
        if (position >= exceptionAt) {
          triggered = true;
          onError(new BrokenIOException(
            format("broken read triggered, size:[%s], exceptionAt:[%s]", totalLength, exceptionAt),
            exceptionAt));
        } else {
          position += item.getSize();
          subscriber.onNext(item);
        }
      }

      @Override
      public void onError(Throwable t) {
        subscriber.onError(t);
      }

      @Override
      public void onComplete() {
        if (triggered) {
          return;
        }
        subscriber.onComplete();
      }
    });
  }

  @Getter
  public static class BrokenIOException extends IOException {
    private final long exceptionAt;

    public BrokenIOException(String message, long exceptionAt) {
      super(message);
      this.exceptionAt = exceptionAt;
    }
  }
}
