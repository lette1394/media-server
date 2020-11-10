package io.lette1394.mediaserver.storage.domain;

import java.util.Optional;
import org.reactivestreams.Publisher;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class BinarySupplierFactory {

  public static <BUFFER extends Payload> BinaryPublisher<BUFFER> from(Publisher<BUFFER> publisher,
    Optional<Long> length) {
    return new BinaryPublisher<BUFFER>() {
      @Override
      public Publisher<BUFFER> publisher() {
        return publisher;
      }

      @Override
      public Optional<Long> length() {
        return length;
      }
    };
  }
}
