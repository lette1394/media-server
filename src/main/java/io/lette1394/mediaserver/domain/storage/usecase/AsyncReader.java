package io.lette1394.mediaserver.domain.storage.usecase;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import lombok.RequiredArgsConstructor;

public abstract class AsyncReader<FROM, TO> {
  private final CompletableFuture<TO> future = new CompletableFuture<>();
  private final long itemLength;

  public AsyncReader(long itemLength) {
    this.itemLength = itemLength;
  }

  public CompletableFuture<TO> read(Publisher<FROM> publisher) {
    publisher.subscribe(new AutoByteBufferSubscriber<>(this));
    return future;
  }

  protected abstract void translateNext(FROM item);

  protected abstract TO translateCompleted();

  @RequiredArgsConstructor
  private static class AutoByteBufferSubscriber<FROM, TO> implements Subscriber<FROM> {

    private final AsyncReader<FROM, TO> reader;

    private Subscription subscription;

    @Override
    public void onSubscribe(Subscription subscription) {
      this.subscription = subscription;
      subscription.request(reader.itemLength);
    }

    @Override
    public void onNext(FROM item) {
      reader.translateNext(item);

      subscription.request(reader.itemLength);
    }

    @Override
    public void onError(Throwable reason) {
      reader.future.completeExceptionally(reason);
    }

    @Override
    public void onComplete() {
      reader.future.complete(reader.translateCompleted());
    }
  }
}
