package io.lette1394.mediaserver.storage;

import io.lette1394.mediaserver.storage.domain.BinaryPath;
import io.lette1394.mediaserver.storage.infrastructure.StringPayload;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class StringInMemoryRepository extends InMemoryStorage<StringPayload> {
  @Override
  protected Publisher<StringPayload> read(BinaryPath binaryPath) {
    return Mono.just(new StringPayload(new String(this.getBinary(binaryPath))));
  }

  @Override
  protected void write(BinaryPath binaryPath, StringPayload item) {
    appendBinary(binaryPath, item.getValue().getBytes());
  }
}
