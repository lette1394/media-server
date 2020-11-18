package io.lette1394.mediaserver.storage.usecase;

import io.lette1394.mediaserver.storage.domain.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.BinaryPublisher;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectRepository;
import io.lette1394.mediaserver.storage.domain.Payload;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Downloading<P extends Payload> {
  private final BinaryRepository<P> binaryRepository;
  private final ObjectRepository<P> objectRepository;

  public CompletableFuture<BinaryPublisher<P>> download(Identifier identifier) {
    return objectRepository
      .find(identifier)
      .thenCompose(Object::download);
  }
}
