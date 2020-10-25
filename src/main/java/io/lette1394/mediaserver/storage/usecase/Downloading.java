package io.lette1394.mediaserver.storage.usecase;

import io.lette1394.mediaserver.storage.domain.object.Identifier;
import io.lette1394.mediaserver.storage.domain.Storage;
import io.lette1394.mediaserver.storage.domain.object.Object;
import java.util.concurrent.CompletableFuture;
import lombok.Value;

@Value
public class Downloading {
  Storage storage;

  public CompletableFuture<Object> download(Command command) throws ObjectNotFoundException {
    return storage.findObject(command.identifier);
  }

  @Value
  public static class Command {
    Identifier identifier;
  }
}
