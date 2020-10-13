package io.lette1394.mediaserver.domain.storage.usecase;

import io.lette1394.mediaserver.domain.storage.Identifier;
import io.lette1394.mediaserver.domain.storage.Object;
import java.util.concurrent.CompletableFuture;
import lombok.Value;

@Value
public class DownloadingAsync {

  @Value
  public static class Command {
    Identifier identifier;
  }

  AsyncStorage storage;

  public CompletableFuture<Object> download(Command command) {
    return storage.findAsync(command.identifier);
  }
}
