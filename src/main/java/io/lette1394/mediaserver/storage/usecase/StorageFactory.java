package io.lette1394.mediaserver.storage.usecase;

import io.lette1394.mediaserver.storage.domain.binary.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.Storage;

class StorageFactory {

  static Storage create(BinarySupplier binarySupplier) {
//    return new InMemoryStorage(() -> new InputStreamPublisher(dataSupplier.get(), 5));
    return null;
  }


}
