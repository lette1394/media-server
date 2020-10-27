package io.lette1394.mediaserver.storage;

import io.lette1394.mediaserver.storage.domain.binary.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.binary.LengthAwareBinarySupplier;
import io.lette1394.mediaserver.storage.infrastructure.SingleThreadInputStreamPublisher;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import lombok.Value;
import org.apache.commons.lang3.RandomUtils;
import org.reactivestreams.Publisher;

@Value
public class TestBinarySupplier implements LengthAwareBinarySupplier {

  private final static int CHUNK_SIZE = 1024;
  byte[] binary;

  public static BinarySupplier randomBinarySupplier(int size) {
    return new TestBinarySupplier(RandomUtils.nextBytes(size));
  }

  public static BinarySupplier randomBinarySupplier() {
    return randomBinarySupplier(1024);
  }

  @Override
  public long getLength() {
    return binary.length;
  }

  @Override
  public Publisher<ByteBuffer> getAsync() {
    return new SingleThreadInputStreamPublisher(new ByteArrayInputStream(binary), CHUNK_SIZE);
  }
}
