package io.lette1394.mediaserver.storage.usecase;

import io.lette1394.mediaserver.storage.domain.Storage;
import io.lette1394.mediaserver.storage.domain.binary.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.object.Identifier;
import io.lette1394.mediaserver.storage.domain.object.Object;
import java.util.concurrent.CompletableFuture;
import lombok.Value;

@Value
public class DownloadingChunked {
  Storage<BinarySupplier> storage;

  // TODO: checked exception
  public CompletableFuture<BinarySupplier> download(Identifier identifier) {
    return storage.findObject(identifier)
      .thenCompose(Object::download);
  }
}
