package io.lette1394.mediaserver.storage.domain;

import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@RequiredArgsConstructor
class ListenablePublisher<P extends Payload> implements Publisher<P> {
  private final Listener listener;
  private final Publisher<P> delegate;

  @Override
  public void subscribe(Subscriber<? super P> subscriber) {
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

  private static class ListenableSubscriber<P extends Payload>
    extends ProcessedLengthAwareSubscriber<P> {

    private final Listener listener;
    private boolean abortNotified = false;

    public ListenableSubscriber(Listener listener, Subscriber<? super P> subscriber) {
      super(subscriber);
      this.listener = listener;
    }

    @Override
    public void onSubscribe(Subscription s) {
      listener.beforeTransfer();
      super.onSubscribe(s);
    }

    @Override
    public void onNext(P payload) {
      super.onNext(payload);
      listener.duringTransferring(getProcessedLength());
    }

    @Override
    public void onError(Throwable t) {
      if (abortNotified) {
        return;
      }
      abortNotified = true;
      listener.transferAborted(t);
      super.onError(t);
    }

    @Override
    public void onComplete() {
      listener.afterTransferred(getProcessedLength());
      super.onComplete();
    }
  }
}
