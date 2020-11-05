package io.lette1394.mediaserver.storage.domain;

import org.reactivestreams.Subscriber;

public class ProcessedLengthAwareSubscriber<BUFFER extends Payload> extends DelegatingSubscriber<BUFFER> {
  private long processedLength = 0L;

  public ProcessedLengthAwareSubscriber(Subscriber<? super BUFFER> subscriber) {
    super(subscriber);
  }

  @Override
  public void onNext(BUFFER buffer) {
    super.onNext(buffer);
    processedLength += buffer.getSize();
  }

  protected long getProcessedLength() {
    return processedLength;
  }
}
