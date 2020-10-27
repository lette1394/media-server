package io.lette1394.mediaserver.storage.domain.binary;

import java.nio.ByteBuffer;
import org.reactivestreams.Publisher;

public class BinarySuppliers {

  public static LengthAwareBinarySupplier convert(BinarySupplier binarySupplier, long length) {
    return new LengthAwareBinarySupplier() {
      @Override
      public long getLength() {
        return length;
      }

      @Override
      public Publisher<ByteBuffer> getAsync() {
        return binarySupplier.getAsync();
      }
    };
  }
}
