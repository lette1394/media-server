package io.lette1394.mediaserver.processing.usecase;

import io.lette1394.mediaserver.common.Result;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.Storage;
import io.lette1394.mediaserver.storage.usecase.Downloading;
import io.lette1394.mediaserver.storage.usecase.Downloading.Command;
import io.lette1394.mediaserver.storage.usecase.ObjectNotFoundException;
import lombok.Value;

@Value
public class DownloadingMedia {
  Storage storage;

  public Object download(Command command) throws ObjectNotFoundException {
    final Result<Object> join = storage.findObject(command.identifier).join();
    final Object object = join.get();


    return null;
  }

  @Value
  public static class Command {
    Identifier identifier;
  }
}
