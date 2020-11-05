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
class ControllablePublisher<BUFFER extends Payload> implements Publisher<BUFFER> {
  private final Policy policy;
  private final Publisher<BUFFER> delegate;

  @Override
  public void subscribe(Subscriber<? super BUFFER> subscriber) {
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

  private static class ControllableSubscriber<BUFFER extends Payload>
    extends ProcessedLengthAwareSubscriber<BUFFER> {

    private final Policy policy;
    private boolean denied = false;

    public ControllableSubscriber(Policy policy, Subscriber<? super BUFFER> subscriber) {
      super(subscriber);
      this.policy = policy;
    }

    @Override
    public void onSubscribe(Subscription s) {
      check(policy::beforeTransfer, () -> super.onSubscribe(s));
    }

    @Override
    public void onNext(BUFFER buffer) {
      check(buffer.getSize(), policy::duringTransferring, () -> super.onNext(buffer));
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
