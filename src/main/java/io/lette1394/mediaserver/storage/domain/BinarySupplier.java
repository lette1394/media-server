package io.lette1394.mediaserver.storage.domain;

import java.io.InputStream;
import java.nio.ByteBuffer;
import org.reactivestreams.Publisher;

public interface BinarySupplier {
  boolean isSyncSupported();

  boolean isAsyncSupported();

  long getSize();

  InputStream getSync();

  Publisher<ByteBuffer> getAsync();

  interface Listener {
    void beforeTransfer();

    void duringTransferring(long currentSize, long total);

    void afterTransferred(long totalLength);
  }
}
