package io.lette1394.mediaserver.domain.storage.usecase;

import io.lette1394.mediaserver.domain.storage.Identifier;
import io.lette1394.mediaserver.domain.storage.Object;
import lombok.Value;

@Value
public class Downloading {

  @Value
  public static class Command {
    Identifier identifier;
  }

  Storage storage;

  public Object download(Command command) throws ObjectNotFoundException {
    return storage.find(command.identifier);
  }
}
