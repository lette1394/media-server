package io.lette1394.mediaserver.domain.storage.usecase;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class ByteBufferToByteArrayAsyncReader extends AsyncReader<ByteBuffer, byte[]> {
  private final ByteArrayOutputStream out = new ByteArrayOutputStream(1024 * 100);

  public ByteBufferToByteArrayAsyncReader(long itemLength) {
    super(itemLength);
  }

  @Override
  protected void translateNext(ByteBuffer item) {
    byte[] bytes = new byte[item.remaining()];
    item.get(bytes);
    out.writeBytes(bytes);
  }

  @Override
  protected byte[] translateCompleted() {
    return out.toByteArray();
  }
}
