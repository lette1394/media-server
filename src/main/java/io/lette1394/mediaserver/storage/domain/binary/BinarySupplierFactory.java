package io.lette1394.mediaserver.storage.domain.binary;

import java.nio.ByteBuffer;
import org.reactivestreams.Publisher;

public class BinarySupplierFactory {

  public static BinarySupplier from(Publisher<ByteBuffer> publisher, long length) {
    return new BaseLengthAwareBinarySupplier() {
      @Override
      public Publisher<ByteBuffer> getAsync()
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
