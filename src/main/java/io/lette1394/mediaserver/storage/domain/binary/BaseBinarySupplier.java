package io.lette1394.mediaserver.storage.domain.binary;

import java.io.InputStream;
import java.nio.ByteBuffer;
import org.reactivestreams.Publisher;

public class BaseBinarySupplier implements BinarySupplier {
  @Override
  public boolean isSyncSupported() {
    return false;
  }

  @Override
  public boolean isAsyncSupported() {
    return false;
  }

  @Override
  public InputStream getSync() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Publisher<ByteBuffer> getAsync() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }
}
