package io.lette1394.mediaserver.domain.storage.usecase;

import io.lette1394.mediaserver.common.AbstractionBoundary;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Flow.Publisher;

@AbstractionBoundary
public interface BinarySupplier {
  boolean isSyncSupported();
  boolean isAsyncSupported();

  InputStream getSync();
  Publisher<ByteBuffer> getAsync();
}
