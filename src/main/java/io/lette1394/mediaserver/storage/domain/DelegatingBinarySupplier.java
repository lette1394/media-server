package io.lette1394.mediaserver.storage.domain;

import java.io.InputStream;
import java.nio.ByteBuffer;
import org.reactivestreams.Publisher;

public class DelegatingBinarySupplier implements BinarySupplier {
  private final BinarySupplier binarySupplier;

  public DelegatingBinarySupplier(BinarySupplier binarySupplier) {
    this.binarySupplier = binarySupplier;
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
  public long getLength() {
    return binarySupplier.getLength();
  }

  @Override
  public InputStream getSync() {
    return binarySupplier.getSync();
  }

  @Override
  public Publisher<ByteBuffer> getAsync() {
    return binarySupplier.getAsync();
  }
}
