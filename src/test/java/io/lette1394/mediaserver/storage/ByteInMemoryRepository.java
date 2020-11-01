package io.lette1394.mediaserver.storage;

import io.lette1394.mediaserver.storage.domain.BinaryPath;
import io.lette1394.mediaserver.storage.infrastructure.BytePayload;
import java.io.ByteArrayOutputStream;
import java.util.stream.IntStream;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

public class ByteInMemoryRepository extends InMemoryStorage<BytePayload> {
  @Override
  protected Publisher<BytePayload> read(BinaryPath binaryPath) {
    final ByteArrayOutputStream outputStream = binaryHolder.get(binaryPath.asString());
    final byte[] bytes = outputStream.toByteArray();

    return Flux.fromStream(
      IntStream
        .range(0, bytes.length)
        .mapToObj(i -> new BytePayload(bytes[i])));
  }

  @Override
  protected void write(BinaryPath binaryPath, BytePayload item) {
    if (!binaryHolder.containsKey(binaryPath.asString())) {
      binaryHolder.put(binaryPath.asString(), new ByteArrayOutputStream());
    }
    final ByteArrayOutputStream outputStream = binaryHolder.get(binaryPath.asString());
    outputStream.write(item.getValue());
  }
}
