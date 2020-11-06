package io.lette1394.mediaserver.storage.domain;

import java.util.Optional;
import org.reactivestreams.Publisher;

public class BinarySupplierFactory {

  public static <BUFFER extends Payload> BinarySupplier<BUFFER> from(Publisher<BUFFER> publisher,
    Optional<Long> length) {
    return new BinarySupplier<BUFFER>() {
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
