package io.lette1394.mediaserver.storage.infrastructure;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class ByteBufferToByteArrayAsyncAggregateReader extends AsyncAggregateReader<ByteBuffer, byte[]> {
  private final ByteArrayOutputStream out = new ByteArrayOutputStream(1024 * 100);

  public ByteBufferToByteArrayAsyncAggregateReader(long itemLength) {
    super(itemLength);
  }

  @Override
  protected void aggregateNext(ByteBuffer item) {
    byte[] bytes = new byte[item.remaining()];
    item.get(bytes);
    out.writeBytes(bytes);
  }

  @Override
  protected byte[] aggregateCompleted() {
    return out.toByteArray();
  }
}
