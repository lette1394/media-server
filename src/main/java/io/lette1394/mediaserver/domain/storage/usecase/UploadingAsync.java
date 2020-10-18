package io.lette1394.mediaserver.domain.storage.usecase;

import io.lette1394.mediaserver.common.Result;
import io.lette1394.mediaserver.domain.storage.object.BinarySupplier;
import io.lette1394.mediaserver.domain.storage.object.Object;
import io.lette1394.mediaserver.domain.storage.object.Storage;
import java.util.concurrent.CompletableFuture;
import lombok.Value;

@Value
public class UploadingAsync {

  @Value
  public static class Command {
    String area;
    String key;
    BinarySupplier dataProvider;
  }

  Storage storage;

  public CompletableFuture<Result> upload(Command command) {
    return null;
  }
}
