package io.lette1394.mediaserver.storage.domain;

import org.reactivestreams.Publisher;

public class BinarySupplierFactory {

  public static <BUFFER extends Payload> BinarySupplier<BUFFER> from(Publisher<BUFFER> publisher, long length) {
    return new BaseLengthAwareBinarySupplier<>() {
      @Override
      public Publisher<BUFFER> getAsync()
        throws UnsupportedOperationException {
        return publisher;
      }

      @Override
      public long getLength() {
        return length;
      }
    };
  }
}
