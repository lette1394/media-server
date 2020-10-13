package io.lette1394.mediaserver.domain.storage.usecase;

import io.lette1394.mediaserver.common.Result;
import io.lette1394.mediaserver.domain.storage.Object;
import lombok.Value;

@Value
public class Uploading {
  @Value
  public static class Command {
    String area;
    String key;
    DataSupplier dataSupplier;
  }

  public Result upload(Command command) {
    final Object object = Object.create("1", "2");

    final Storage storage = StorageFactory.create(command.dataSupplier);
    return storage.upload(object);
  }
}
