package io.lette1394.mediaserver.processing.instrastructure;

import io.lette1394.mediaserver.processing.domain.MediaAwareBinaryPublisher;
import io.lette1394.mediaserver.storage.domain.BinaryPublisher;
import io.lette1394.mediaserver.storage.infrastructure.DataBufferPayload;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import lombok.SneakyThrows;
import org.springframework.core.io.buffer.DataBuffer;

public class DataBufferMediaAwareBinaryPublisher extends MediaAwareBinaryPublisher<DataBufferPayload> {
  public DataBufferMediaAwareBinaryPublisher(BinaryPublisher<DataBufferPayload> delegate, Listener listener) {
    super(delegate, listener);
  }

  @Override
  @SneakyThrows
  protected byte[] getByte(DataBufferPayload payload) {
    final DataBuffer retainedSlice = payload.getValue().retainedSlice(0, (int) payload.getSize());
    byte[] ret = new byte[(int) payload.getSize()];

    try(final InputStream inputStream = retainedSlice.asInputStream(true)) {
      inputStream.read(ret);
    }
    return ret;
  }
}
