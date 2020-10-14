package io.lette1394.mediaserver.domain.storage.usecase;

import io.lette1394.mediaserver.domain.storage.object.Identifier;
import io.lette1394.mediaserver.domain.storage.object.Object;
import lombok.Value;

@Value
public class Downloading {

  @Value
  public static class Command {
    Identifier identifier;
  }

  Storage storage;

  public Object download(Command command) throws ObjectNotFoundException {
    final StorageResult<Object> objectStorageResult = storage.find(command.identifier);
    return null;
  }
}
