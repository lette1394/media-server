package io.lette1394.mediaserver.domain.storage.usecase;

import io.lette1394.mediaserver.common.Result;
import io.lette1394.mediaserver.domain.storage.object.BinarySupplier;
import io.lette1394.mediaserver.domain.storage.object.Object;
import io.lette1394.mediaserver.domain.storage.object.ObjectFactory;
import io.lette1394.mediaserver.domain.storage.object.Storage;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.Value;

@Value
public class Uploading {
  Storage storage;

  public CompletableFuture<Result> upload(Command command) {
    final ObjectFactory factory = new ObjectFactory(storage);
    final Object object = factory.create(command.area, command.key);

    return object.upload(command.binarySupplier);
  }

  @Value
  public static class Command {
    String area;
    String key;
    Map<String, String> tags;
    BinarySupplier binarySupplier;
  }
}
