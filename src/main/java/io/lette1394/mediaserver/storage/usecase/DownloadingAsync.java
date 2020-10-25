package io.lette1394.mediaserver.storage.usecase;

import io.lette1394.mediaserver.storage.domain.object.Identifier;
import io.lette1394.mediaserver.storage.domain.object.Object;
import io.lette1394.mediaserver.storage.domain.Storage;
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
