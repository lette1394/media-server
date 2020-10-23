package io.lette1394.mediaserver.storage.usecase;

import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.Storage;
import lombok.Value;

@Value
public class Downloading {
  Storage storage;

  public Object download(Command command) throws ObjectNotFoundException {
    storage.findObject(command.identifier);
    return null;
  }

  @Value
  public static class Command {
    Identifier identifier;
  }
}
