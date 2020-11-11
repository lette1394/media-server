package io.lette1394.mediaserver.processing.instrastructure;

import io.lette1394.mediaserver.processing.domain.MediaAwareBinaryPublisher;
import io.lette1394.mediaserver.storage.domain.BinaryPublisher;
import io.lette1394.mediaserver.storage.infrastructure.DataBufferPayload;
import lombok.SneakyThrows;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;

public class DataBufferMediaAwareBinaryPublisher extends MediaAwareBinaryPublisher<DataBufferPayload> {
  public DataBufferMediaAwareBinaryPublisher(BinaryPublisher<DataBufferPayload> delegate, Listener listener) {
    super(delegate, listener);
  }

  @Override
  @SneakyThrows
  protected byte[] getByte(DataBufferPayload payload) {
    final DataBuffer dataBuffer = payload.getValue().retainedSlice(0, (int) payload.getSize());

    DataBufferUtils.retain(payload.getValue());
    DataBufferUtils.retain(payload.getValue());
    DataBufferUtils.retain(payload.getValue());
    DataBufferUtils.retain(payload.getValue());
    DataBufferUtils.retain(payload.getValue());
    DataBufferUtils.retain(payload.getValue());
    DataBufferUtils.retain(dataBuffer);
    DataBufferUtils.retain(dataBuffer);
    DataBufferUtils.retain(dataBuffer);
    DataBufferUtils.retain(dataBuffer);
    DataBufferUtils.retain(dataBuffer);
    DataBufferUtils.retain(dataBuffer);
    DataBufferUtils.retain(dataBuffer);


    byte[] ret = new byte[(int) payload.getSize()];
    dataBuffer.asInputStream(false).read(ret);
    return ret;
  }
}
