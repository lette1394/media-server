package io.lette1394.mediaserver.domain.storage.usecase;

import io.lette1394.mediaserver.domain.storage.object.BinarySupplier;
import io.lette1394.mediaserver.domain.storage.object.Object;
import io.lette1394.mediaserver.domain.storage.object.ObjectFactory;
import io.lette1394.mediaserver.domain.storage.object.Storage;
import lombok.Value;

@Value
public class Uploading {
  @Value
  public static class Command {
    String area;
    String key;
    BinarySupplier binarySupplier;
  }

  Storage storage;

  public StorageResult<Void> upload(Command command) {
    final ObjectFactory factory = new ObjectFactory(storage);
    final Object object = factory.create("1", "2");

    return storage.upload(object, command.binarySupplier);
  }
}
