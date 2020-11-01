package io.lette1394.mediaserver.storage.usecase;

import io.lette1394.mediaserver.storage.domain.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectRepository;
import io.lette1394.mediaserver.storage.domain.Payload;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Downloading<BUFFER extends Payload> {
  private final BinaryRepository<BUFFER> binaryRepository;
  private final ObjectRepository<BUFFER> objectRepository;

  public CompletableFuture<BinarySupplier<BUFFER>> download(Identifier identifier) {
    return objectRepository
      .find(identifier)
      .thenCompose(Object::download);
  }
}
