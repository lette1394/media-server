package io.lette1394.mediaserver.domain.storage.infrastructure;

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
public class SingleThreadInputStreamPublisher implements Publisher<ByteBuffer> {
  final ExecutorService executor = Executors.newFixedThreadPool(1);

  private static final int END_OF_FILE = -1;

  private final InputStream inputStream;
  private final int chunkSize;

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
      public void request(long n) { // TODO: n은 item의 개수지 byte수가 아니다!
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
        final byte[] bytes = new byte[chunkSize];
        final int signalOrLength = inputStream.read(bytes, 0, chunkSize);

        if (signalOrLength == END_OF_FILE) {
          onComplete();
          return;
        }
        // TODO: 여기 안에서도 cancle 조건을 체크해야 함

        // TODO: 이거 제대로 된 테스트는 어떻게 하지?
        //  spring flux 툴로 할 수 있을 거 같은데... 찾아보자
        //  step verifier !!! https://www.baeldung.com/reactive-streams-step-verifier-test-publisher

        // TODO: signalOrLength 고치기
        onNext(bytes, signalOrLength);

        remaining -= 1;
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
