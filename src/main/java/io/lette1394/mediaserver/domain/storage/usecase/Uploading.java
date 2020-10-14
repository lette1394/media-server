package io.lette1394.mediaserver.domain.storage.usecase;

import io.lette1394.mediaserver.domain.storage.object.Object;
import lombok.Value;

@Value
public class Uploading {
  @Value
  public static class Command {
    String area;
    String key;
    BinarySupplier binarySupplier;
  }

  public StorageResult<BinarySupplier> upload(Command command) {
    final Object object = Object.create("1", "2");

    final Storage storage = StorageFactory.create(command.binarySupplier);

    return storage.download(object);
  }
}
