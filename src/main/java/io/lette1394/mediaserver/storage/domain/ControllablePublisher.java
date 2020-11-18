package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.Tries;
import io.vavr.control.Try;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@RequiredArgsConstructor
class ControllablePublisher<P extends Payload> implements Publisher<P> {
  private final Policy policy;
  private final Publisher<P> delegate;

  @Override
  public void subscribe(Subscriber<? super P> subscriber) {
    delegate.subscribe(new ControllableSubscriber<>(policy, subscriber));
  }

  public interface Policy {

    default Try<Void> beforeTransfer() {
      return Tries.SUCCESS;
    }

    default Try<Void> duringTransferring(long currentLength) {
      return Tries.SUCCESS;
    }

    default Try<Void> afterTransferred(long totalLength) {
      return Tries.SUCCESS;
    }
  }

  private static class ControllableSubscriber<P extends Payload>
    extends ProcessedLengthAwareSubscriber<P> {

    private final Policy policy;
    private boolean denied = false;

    public ControllableSubscriber(Policy policy, Subscriber<? super P> subscriber) {
      super(subscriber);
      this.policy = policy;
    }

    @Override
    public void onSubscribe(Subscription s) {
      check(policy::beforeTransfer, () -> super.onSubscribe(s));
    }

    @Override
    public void onNext(P payload) {
      check(payload.getSize(), policy::duringTransferring, () -> super.onNext(payload));
    }

    @Override
    public void onComplete() {
      check(0L, policy::afterTransferred, () -> super.onComplete());
    }

    private void check(Supplier<Try<Void>> predicate, Runnable whenAllowed) {
      check(0L, __ -> predicate.get(), whenAllowed);
    }

    private void check(long currentLength, Function<Long, Try<Void>> predicate,
      Runnable whenAllowed) {
      if (denied) {
        return;
      }

      final Try<Void> result = predicate.apply(currentLength + getProcessedLength());
      if (result.isSuccess()) {
        whenAllowed.run();
        return;
      }

      denied = true;
      onError(result.getCause());
    }
  }
}
