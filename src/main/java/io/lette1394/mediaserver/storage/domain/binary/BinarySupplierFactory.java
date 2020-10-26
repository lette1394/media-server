package io.lette1394.mediaserver.storage.domain.binary;

import java.io.InputStream;
import java.nio.ByteBuffer;
import org.reactivestreams.Publisher;

public class BinarySupplierFactory {
  public static BinarySupplier from(InputStream inputStream, long length) {
    return new BaseLengthAwareBinarySupplier() {
      @Override
      public boolean isSyncSupported() {
        return true;
      }

      @Override
      public InputStream getSync() throws UnsupportedOperationException {
        return inputStream;
      }

      @Override
      public long getLength() {
        return length;
      }
    };
  }

  public static BinarySupplier from(InputStream inputStream) {
    return new BaseBinarySupplier() {
      @Override
      public boolean isSyncSupported() {
        return true;
      }

      @Override
      public InputStream getSync() throws UnsupportedOperationException {
        return inputStream;
      }
    };
  }

  public static BinarySupplier from(Publisher<ByteBuffer> publisher, long length) {
    return new BaseLengthAwareBinarySupplier() {
      @Override
      public boolean isAsyncSupported() {
        return true;
      }

      @Override
      public org.reactivestreams.Publisher<ByteBuffer> getAsync()
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
