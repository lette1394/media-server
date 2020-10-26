package io.lette1394.mediaserver.storage.domain.binary;

import java.io.InputStream;
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
      public boolean isSyncSupported() {
        return binarySupplier.isSyncSupported();
      }

      @Override
      public boolean isAsyncSupported() {
        return binarySupplier.isAsyncSupported();
      }

      @Override
      public InputStream getSync() throws UnsupportedOperationException {
        return binarySupplier.getSync();
      }

      @Override
      public Publisher<ByteBuffer> getAsync() throws UnsupportedOperationException {
        return binarySupplier.getAsync();
      }
    };
  }
}
