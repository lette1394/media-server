package io.lette1394.mediaserver.storage;

import io.lette1394.mediaserver.storage.domain.BinaryPath;
import io.lette1394.mediaserver.storage.infrastructure.BytePayload;
import java.util.stream.IntStream;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

public class ByteInMemoryRepository extends InMemoryStorage<BytePayload> {
  @Override
  protected Publisher<BytePayload> read(BinaryPath binaryPath) {
    final byte[] bytes = this.getBinary(binaryPath);
    return Flux.fromStream(
      IntStream
        .range(0, bytes.length)
        .mapToObj(i -> new BytePayload(bytes[i])));
  }

  @Override
  protected void write(BinaryPath binaryPath, BytePayload item) {
    appendBinary(binaryPath, new byte[]{item.getValue()});
  }
}
