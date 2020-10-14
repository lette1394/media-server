package io.lette1394.mediaserver.domain.storage.usecase;

import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class SingleThreadInputStreamPublisher implements Publisher<ByteBuffer> {
  final ExecutorService executor = Executors.newFixedThreadPool(1);

  private static final int END_OF_FILE = -1;

  private final InputStream inputStream;
  private final long chunkSize;

  boolean isCanceled = false;
  boolean isCompleted = false;
  Subscriber<? super ByteBuffer> subscriber;


  @Override
  public void subscribe(Subscriber<? super ByteBuffer> subscriber) {
    this.subscriber = subscriber;
    subscriber.onSubscribe(newSubscription());
  }

  private Subscription newSubscription() {
    return new Subscription() {
      @Override
      public void request(long n) {
        executor.submit(() -> read(n));
      }

      @Override
      public void cancel() {
        markCanceled();
      }
    };
  }

  private void read(long n) {
    if (n <= 0) {
      onError(new IllegalArgumentException(format("request must be more than 0, got: %s", n)));
      return;
    }

    if (isCanceled) {
      onError(new IllegalAccessException("Canceled subscription"));
      return;
    }

    pump(n);
  }

  private void pump(long n) {
    long remaining = n;

    try {
      while (remaining > 0) {
        final int chunk = (int)Math.min(remaining, chunkSize);
        final byte[] bytes = new byte[chunk];
        final int signalOrLength = inputStream.read(bytes, 0, chunk);

        if (signalOrLength == END_OF_FILE) {
          onComplete();
          return;
        }

        onNext(bytes, signalOrLength);

        remaining -= chunkSize;
      }
    } catch (IOException e) {
      onError(e);
    }
  }

  private void markCanceled() {
    isCanceled = true;
  }

  private void onError(Throwable throwable) {
    subscriber.onError(throwable);
  }

  private void onNext(byte[] bytes, int length) {
    subscriber.onNext(ByteBuffer.wrap(bytes, 0, length));
  }

  private void onComplete() {
    isCompleted = true;
    subscriber.onComplete();
  }
}
