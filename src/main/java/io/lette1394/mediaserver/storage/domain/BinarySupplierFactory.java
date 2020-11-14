package io.lette1394.mediaserver.storage.domain;

import java.util.Optional;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class BinarySupplierFactory {
  public static <P extends Payload> BinaryPublisher<P> from(
    Publisher<P> publisher,
    Optional<Long> length) {

    return new BinaryPublisher<P>() {
      @Override
      public void subscribe(Subscriber<? super P> s) {
        publisher.subscribe(s);
      }

      @Override
      public Optional<Long> length() {
        return length;
      }
    };
  }
}
