package io.lette1394.mediaserver.domain.storage.usecase;

import io.lette1394.mediaserver.domain.storage.object.BinarySupplier;
import io.lette1394.mediaserver.domain.storage.object.Storage;

class StorageFactory {
  static Storage create(BinarySupplier binarySupplier) {
//    return new InMemoryStorage(() -> new InputStreamPublisher(dataSupplier.get(), 5));
    return null;
  }


}
