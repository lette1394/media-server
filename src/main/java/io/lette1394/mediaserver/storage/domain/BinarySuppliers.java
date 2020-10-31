package io.lette1394.mediaserver.storage.domain;

import org.reactivestreams.Publisher;

public class BinarySuppliers {

  public static <BUFFER extends Payload> LengthAwareBinarySupplier<BUFFER> convert(BinarySupplier<BUFFER> binarySupplier, long length) {
    return new LengthAwareBinarySupplier<>() {
      @Override
      public long getLength() {
        return length;
      }

      @Override
      public Publisher<BUFFER> getAsync() {
        return binarySupplier.getAsync();
      }
    };
  }
}
