package io.lette1394.mediaserver.domain.storage;

import io.lette1394.mediaserver.domain.storage.infrastructure.SingleThreadInputStreamPublisher;
import io.lette1394.mediaserver.domain.storage.object.BinarySupplier;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import lombok.Value;
import org.apache.commons.lang3.RandomUtils;
import org.reactivestreams.Publisher;

@Value
public class TestBinarySupplier implements BinarySupplier {
  private final static int CHUNK_SIZE = 1024;
  byte[] binary;

  public static BinarySupplier randomBinarySupplier(int size) {
    return new TestBinarySupplier(RandomUtils.nextBytes(size));
  }

  public static BinarySupplier randomBinarySupplier() {
    return randomBinarySupplier(1024);
  }

  @Override
  public boolean isSyncSupported() {
    return true;
  }

  @Override
  public boolean isAsyncSupported() {
    return true;
  }

  @Override
  public long length() {
    return binary.length;
  }

  @Override
  public InputStream getSync() {
    return new ByteArrayInputStream(binary);
  }

  @Override
  public Publisher<ByteBuffer> getAsync() {
    return new SingleThreadInputStreamPublisher(new ByteArrayInputStream(binary), CHUNK_SIZE);
  }
}
