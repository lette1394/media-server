package io.lette1394.mediaserver.storage.domain.binary;

import java.nio.ByteBuffer;
import org.reactivestreams.Publisher;

public class BaseBinarySupplier implements BinarySupplier {

  @Override
  public Publisher<ByteBuffer> getAsync() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }
}
