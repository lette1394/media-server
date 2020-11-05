package io.lette1394.mediaserver.storage.domain;

import static io.lette1394.mediaserver.common.Violations.violation;
import static java.lang.String.format;

import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

@RequiredArgsConstructor
public class ValidatingLengthPublisher<BUFFER extends Payload> implements Publisher<BUFFER> {
  private final long expectedTotalLength;
  private final Publisher<BUFFER> delegate;

  @Override
  public void subscribe(Subscriber<? super BUFFER> subscriber) {
    delegate.subscribe(new ValidatingLengthSubscriber<>(expectedTotalLength, subscriber));
  }

  private static class ValidatingLengthSubscriber<BUFFER extends Payload> extends
    ProcessedLengthAwareSubscriber<BUFFER> {

    private final long expectedTotalLength;

    public ValidatingLengthSubscriber(
      long expectedTotalLength,
      Subscriber<? super BUFFER> subscriber) {

      super(subscriber);
      this.expectedTotalLength = expectedTotalLength;
    }

    @Override
    public void onComplete() {
      if (expectedTotalLength == getProcessedLength()) {
        super.onComplete();
        return;
      }

      onError(violation(
        format("publisher total length mismatched; expected:[%s], but:[%s]",
          expectedTotalLength,
          getProcessedLength())));
    }

  }
}
