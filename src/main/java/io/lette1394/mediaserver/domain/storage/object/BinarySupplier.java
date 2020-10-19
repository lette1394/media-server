package io.lette1394.mediaserver.domain.storage.object;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Flow.Publisher;

public interface BinarySupplier {
  boolean isSyncSupported();
  boolean isAsyncSupported();

  InputStream getSync();
  Publisher<ByteBuffer> getAsync();
}
