package io.lette1394.mediaserver.storage.domain;

import org.reactivestreams.Subscriber;

class ProcessedLengthAwareSubscriber<P extends Payload> extends DelegatingSubscriber<P, P> {
  private long processedLength = 0L;

  public ProcessedLengthAwareSubscriber(Subscriber<? super P> subscriber) {
    super(subscriber);
  }

  @Override
  public void onNext(P payload) {
    delegate.onNext(payload);
    processedLength += payload.getSize();
  }

  protected long getProcessedLength() {
    return processedLength;
  }
}
