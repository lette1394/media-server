package io.lette1394.mediaserver.domain.storage.usecase;

import io.lette1394.mediaserver.domain.storage.object.Identifier;
import io.lette1394.mediaserver.domain.storage.object.Object;
import io.lette1394.mediaserver.domain.storage.object.Storage;
import java.util.concurrent.CompletableFuture;
import lombok.Value;

@Value
public class DownloadingAsync {
  Storage storage;

  public CompletableFuture<Object> download(Command command) {
    storage.findObject(command.identifier);
    return null;
  }

  @Value
  public static class Command {
    Identifier identifier;
  }
}
