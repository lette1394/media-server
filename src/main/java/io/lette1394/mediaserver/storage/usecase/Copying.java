package io.lette1394.mediaserver.storage.usecase;

import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectRepository;
import io.lette1394.mediaserver.storage.domain.Payload;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class Copying<BUFFER extends Payload> {
  private final ObjectRepository<BUFFER> objectRepository;
  private final Uploading<BUFFER> uploading;

  public CompletableFuture<Object<BUFFER>> copy(Command command) {
    return objectRepository.find(command.from)
      .thenCompose(object -> object.download())
      .thenCompose(source -> uploading.upload(Uploading.Command.<BUFFER>builder()
        .upstream(source)
        .identifier(command.to)
        .build()));
  }

  @Value
  @Builder
  public static class Command {
    Identifier from;
    Identifier to;
  }
}
