package io.lette1394.mediaserver.domain.storage;

import io.lette1394.mediaserver.domain.storage.object.BinarySupplier;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Flow.Publisher;
import lombok.Value;

@Value
public class SyncBinarySupplier implements BinarySupplier {
  byte[] binary;

  @Override
  public boolean isSyncSupported() {
    return true;
  }

  @Override
  public boolean isAsyncSupported() {
    return false;
  }

  @Override
  public InputStream getSync() {
    return new ByteArrayInputStream(binary);
  }

  @Override
  public Publisher<ByteBuffer> getAsync() {
    throw new UnsupportedOperationException();
  }
}
