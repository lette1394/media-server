package io.lette1394.mediaserver.domain.storage.usecase;

import io.lette1394.mediaserver.domain.storage.object.Identifier;
import io.lette1394.mediaserver.domain.storage.object.Object;
import java.util.concurrent.CompletableFuture;
import lombok.Value;

@Value
public class DownloadingAsync {

  @Value
  public static class Command {
    Identifier identifier;
  }

  Storage storage;

  public CompletableFuture<Object> download(Command command) {
    final StorageResult<Object> objectStorageResult = storage.find(command.identifier);
    return null;
  }
}
