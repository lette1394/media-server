package io.lette1394.mediaserver.storage.domain.binary;

import java.nio.ByteBuffer;
import org.reactivestreams.Publisher;

public class BaseLengthAwareBinarySupplier implements LengthAwareBinarySupplier {

  @Override
  public long getLength() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Publisher<ByteBuffer> getAsync() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }
}
