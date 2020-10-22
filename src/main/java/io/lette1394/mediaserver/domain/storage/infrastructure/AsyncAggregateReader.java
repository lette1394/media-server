package io.lette1394.mediaserver.domain.storage.infrastructure;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import lombok.RequiredArgsConstructor;

public abstract class AsyncAggregateReader<FROM, TO> {
  private final CompletableFuture<TO> future = new CompletableFuture<>();
  private final long itemLength;

  public AsyncAggregateReader(long itemLength) {
    this.itemLength = itemLength;
  }

  public CompletableFuture<TO> read(Publisher<FROM> publisher) {
    publisher.subscribe(new AutoByteBufferSubscriber<>(this));
    return future;
  }

  protected abstract void aggregateNext(FROM item);

  protected abstract TO aggregateCompleted();

  @RequiredArgsConstructor
  private static class AutoByteBufferSubscriber<FROM, TO> implements Subscriber<FROM> {

    private final AsyncAggregateReader<FROM, TO> reader;

    private Subscription subscription;

    @Override
    public void onSubscribe(Subscription subscription) {
      this.subscription = subscription;
      subscription.request(reader.itemLength);
    }

    // TODO: error handling
    @Override
    public void onNext(FROM item) {
      reader.aggregateNext(item);

      subscription.request(reader.itemLength);
    }

    @Override
    public void onError(Throwable reason) {
      reader.future.completeExceptionally(reason);
    }

    // TODO: error handling
    @Override
    public void onComplete() {
      reader.future.complete(reader.aggregateCompleted());
    }
  }
}
