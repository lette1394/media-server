package io.lette1394.mediaserver.storage.usecase;

import io.lette1394.mediaserver.storage.domain.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectRepository;
import io.lette1394.mediaserver.storage.domain.Payload;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class Downloading<BUFFER extends Payload> {
  private final BinaryRepository<BUFFER> binaryRepository;
  private final ObjectRepository<BUFFER> objectRepository;

  public CompletableFuture<BinarySupplier<BUFFER>> download(Command<BUFFER> command) {
    return objectRepository
      .find(command.identifier)
      .thenCompose(Object::download);
  }

  @Value
  @Builder
  public static class Command<BUFFER extends Payload> {
    Identifier identifier;
  }
}
