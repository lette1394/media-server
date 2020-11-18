package io.lette1394.mediaserver.storage;

import io.lette1394.mediaserver.storage.domain.BinaryPath;
import io.lette1394.mediaserver.storage.infrastructure.StringPayload;
import java.io.ByteArrayOutputStream;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class StringInMemoryRepository extends InMemoryStorage<StringPayload> {
  @Override
  protected Publisher<StringPayload> read(BinaryPath binaryPath) {
    final ByteArrayOutputStream outputStream = binaryHolder.get(binaryPath.asString());
    final byte[] bytes = outputStream.toByteArray();

    return Mono.just(new StringPayload(new String(bytes)));
  }

  @Override
  protected void write(BinaryPath binaryPath, StringPayload item) {
    if (!binaryHolder.containsKey(binaryPath.asString())) {
      binaryHolder.put(binaryPath.asString(), new ByteArrayOutputStream());
    }
    final ByteArrayOutputStream outputStream = binaryHolder.get(binaryPath.asString());
    outputStream.writeBytes(item.getValue().getBytes());
  }
}
