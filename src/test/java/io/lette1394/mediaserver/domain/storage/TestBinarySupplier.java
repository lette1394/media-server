package io.lette1394.mediaserver.domain.storage;

import io.lette1394.mediaserver.domain.storage.infrastructure.SingleThreadInputStreamPublisher;
import io.lette1394.mediaserver.domain.storage.object.BinarySupplier;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Flow.Publisher;
import lombok.Value;
import org.apache.commons.lang3.RandomStringUtils;

@Value
public class TestBinarySupplier implements BinarySupplier {
  private final static int CHUNK_SIZE = 1024;
  byte[] binary;

  public static BinarySupplier randomBinarySupplier(int size) {
    return new TestBinarySupplier(RandomStringUtils.random(size).getBytes());
  }

  public static BinarySupplier randomBinarySupplier() {
    return randomBinarySupplier(1000);
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
  public InputStream getSync() {
    return new ByteArrayInputStream(binary);
  }

  @Override
  public Publisher<ByteBuffer> getAsync() {
    return new SingleThreadInputStreamPublisher(new ByteArrayInputStream(binary), CHUNK_SIZE);
  }
}
