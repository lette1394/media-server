package io.lette1394.mediaserver.storage.usecase;

import static io.lette1394.mediaserver.storage.domain.Policies.runNextIfPassedAsync;

import io.lette1394.mediaserver.common.Result;
import io.lette1394.mediaserver.storage.domain.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectFactory;
import io.lette1394.mediaserver.storage.domain.Storage;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.Value;

@Value
public class Uploading {
  Storage storage;

  public CompletableFuture<Result<Void>> upload(Command command) {
    final ObjectFactory factory = new ObjectFactory(storage);
    final Object object = factory.create(command.area, command.key);

    return object.upload(command.binarySupplier)
      .thenCompose(runNextIfPassedAsync(() -> storage.createObject(object)));
  }

  @Value
  public static class Command {
    String area;
    String key;
    Map<String, String> tags;
    BinarySupplier binarySupplier;
  }
}
