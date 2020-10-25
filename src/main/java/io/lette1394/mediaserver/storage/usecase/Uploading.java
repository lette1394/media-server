package io.lette1394.mediaserver.storage.usecase;

import io.lette1394.mediaserver.storage.domain.Storage;
import io.lette1394.mediaserver.storage.domain.binary.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.object.Factory;
import io.lette1394.mediaserver.storage.domain.object.Object;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.Value;

@Value
public class Uploading {
  Storage storage;

  public CompletableFuture<Object> upload(Command command) {
    final Factory factory = new Factory(storage);
    final Object object = factory.create(command.area, command.key);

    return object
      .upload(command.binarySupplier)
      .thenCompose(storage::saveObject);
  }

  @Value
  public static class Command {
    String area;
    String key;
    Map<String, String> tags;
    BinarySupplier binarySupplier;
  }
}
