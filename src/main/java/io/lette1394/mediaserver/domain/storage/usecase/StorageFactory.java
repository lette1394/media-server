package io.lette1394.mediaserver.domain.storage.usecase;

class StorageFactory {
  static AsyncStorage createAsync(AsyncDataSupplier dataSupplier) {
//    return new InMemoryStorage(dataSupplier);
    return null;
  }

  static Storage create(DataSupplier dataSupplier) {
//    return new InMemoryStorage(() -> new InputStreamPublisher(dataSupplier.get(), 5));
    return null;
  }


}
