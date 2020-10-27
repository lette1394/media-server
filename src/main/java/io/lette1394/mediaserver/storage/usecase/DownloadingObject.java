package io.lette1394.mediaserver.storage.usecase;

import io.lette1394.mediaserver.storage.domain.Storage;
import io.lette1394.mediaserver.storage.domain.object.Identifier;
import io.lette1394.mediaserver.storage.domain.object.Object;
import java.util.concurrent.CompletableFuture;
import lombok.Value;

@Value
public class DownloadingObject {
  Storage storage;

  public CompletableFuture<Object> download(Identifier identifier) throws ObjectNotFoundException {
    return storage.findObject(identifier);
  }
}
