package io.lette1394.mediaserver.storage.domain;

import static io.lette1394.mediaserver.common.Violations.Code.MISMATCHED_CONTENT_LENGTH;
import static io.lette1394.mediaserver.common.Violations.violation;
import static java.lang.String.format;

import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

@RequiredArgsConstructor
class ValidatingLengthPublisher<P extends Payload> implements Publisher<P> {
  private final long expectedTotalLength;
  private final Publisher<P> delegate;

  @Override
  public void subscribe(Subscriber<? super P> subscriber) {
    delegate.subscribe(new ValidatingLengthSubscriber<>(expectedTotalLength, subscriber));
  }

  private static class ValidatingLengthSubscriber<P extends Payload> extends
    ProcessedLengthAwareSubscriber<P> {

    private final long expectedTotalLength;

    public ValidatingLengthSubscriber(
      long expectedTotalLength,
      Subscriber<? super P> subscriber) {

      super(subscriber);
      this.expectedTotalLength = expectedTotalLength;
    }

    @Override
    public void onNext(P payload) {
      if (expectedTotalLength >= getProcessedLength()) {
        super.onNext(payload);
        return;
      }
      onError(violation(
        MISMATCHED_CONTENT_LENGTH,
        format("publisher total length exceed; eager aborted; expected:[%s], but:[%s]",
          expectedTotalLength,
          getProcessedLength())));
    }

    @Override
    public void onComplete() {
      if (expectedTotalLength == getProcessedLength()) {
        super.onComplete();
        return;
      }

      onError(violation(
        MISMATCHED_CONTENT_LENGTH,
        format("publisher total length mismatched; expected:[%s], but:[%s]",
          expectedTotalLength,
          getProcessedLength())));
    }
  }
}
