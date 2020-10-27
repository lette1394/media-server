package io.lette1394.mediaserver.storage.usecase;

import static io.lette1394.mediaserver.storage.domain.binary.BinarySuppliers.convert;

import io.lette1394.mediaserver.storage.domain.Storage;
import io.lette1394.mediaserver.storage.domain.binary.LengthAwareBinarySupplier;
import io.lette1394.mediaserver.storage.domain.object.Identifier;
import java.util.concurrent.CompletableFuture;
import lombok.Value;

@Value
public class DownloadingBinary {
  Storage storage;

  public CompletableFuture<LengthAwareBinarySupplier> download(Identifier identifier)
    throws ObjectNotFoundException {
//    return storage
//      .findObject(identifier)
//      .thenCompose(object -> object.download()
//        .thenApply(binarySupplier ->
//          convert(binarySupplier, object.getSnapshot().getSize())));
    return null;
  }
}
