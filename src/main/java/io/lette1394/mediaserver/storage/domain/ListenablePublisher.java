package io.lette1394.mediaserver.storage.domain;

import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@RequiredArgsConstructor
class ListenablePublisher<BUFFER extends Payload> implements Publisher<BUFFER> {
  private final Listener listener;
  private final Publisher<BUFFER> delegate;

  @Override
  public void subscribe(Subscriber<? super BUFFER> subscriber) {
    delegate.subscribe(new ListenableSubscriber<>(listener, subscriber));
  }

  public interface Listener {

    default void beforeTransfer() {
    }

    default void duringTransferring(long currentLength) {
    }

    default void afterTransferred(long totalLength) {
    }

    default void transferAborted(Throwable throwable) {
    }
  }

  private static class ListenableSubscriber<BUFFER extends Payload>
    extends ProcessedLengthAwareSubscriber<BUFFER> {

    private final Listener listener;
    private boolean abortNotified = false;

    public ListenableSubscriber(Listener listener, Subscriber<? super BUFFER> subscriber) {
      super(subscriber);
      this.listener = listener;
    }

    @Override
    public void onSubscribe(Subscription s) {
      listener.beforeTransfer();
      super.onSubscribe(s);
    }

    @Override
    public void onNext(BUFFER buffer) {
      super.onNext(buffer);
      listener.duringTransferring(getProcessedLength());
    }

    @Override
    public void onError(Throwable t) {
      super.onError(t);

      if (abortNotified) {
        return;
      }
      abortNotified = true;
      listener.transferAborted(t);
    }

    @Override
    public void onComplete() {
      listener.afterTransferred(getProcessedLength());
      super.onComplete();
    }
  }
}
