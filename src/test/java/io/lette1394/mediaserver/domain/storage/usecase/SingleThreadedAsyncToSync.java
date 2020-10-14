package io.lette1394.mediaserver.domain.storage.usecase;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.LinkedBlockingQueue;

class SingleThreadedAsyncToSync extends InputStream {
  private static final long REQUEST_COUNT = 5;

  private final BlockingQueue<ByteBuffer> queue;

  private ByteBuffer lastItem;

  private boolean isCompleted = false;

  private boolean isFailed = false;
  private Throwable failureReason = null;

  public SingleThreadedAsyncToSync(Publisher<ByteBuffer> publisher) {
    this.queue = new LinkedBlockingQueue<>();

    publisher.subscribe(createSubscriber());
  }

  private Subscriber<ByteBuffer> createSubscriber() {
    return new Subscriber<>() {
      @Override
      public void onSubscribe(Subscription subscription) {
        subscription.request(REQUEST_COUNT);
      }

      @Override
      public void onNext(ByteBuffer item) {
        offer(item);
      }

      @Override
      public void onError(Throwable throwable) {
        markError(throwable);
        clearQueue();
      }

      @Override
      public void onComplete() {
        markComplete();
      }
    };
  }

  @Override
  public int read() throws IOException {
    ensureLastItemExists();
    checkGotError();

    if (doesAllItemRead()) {
      return -1;
    }
    if (hasLastItemRemaining()) {
      return lastItem.get();
    }

    takeLastItem();
    return lastItem.get();
  }

  private boolean hasLastItemRemaining() {
    return lastItem.remaining() > 0;
  }

  private void ensureLastItemExists() {
    if (lastItem == null) {
      takeLastItem();
    }
  }

  private void takeLastItem() {
    try {
      lastItem = queue.take();
    } catch (InterruptedException e) {
      markError(e);
    }
  }

  private void checkGotError() throws IOException {
    if (isFailed) {
      throw new IOException(failureReason);
    }
  }

  private void offer(ByteBuffer item) {
    queue.offer(item);
  }

  private void clearQueue() {
    queue.clear();
  }

  private void markError(Throwable throwable) {
    isFailed = true;
    failureReason = throwable;
  }

  private void markComplete() {
    isCompleted = true;
  }

  private boolean doesAllItemRead() {
    return isCompleted && queue.isEmpty();
  }
}