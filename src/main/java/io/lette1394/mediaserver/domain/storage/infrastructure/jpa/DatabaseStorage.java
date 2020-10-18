package io.lette1394.mediaserver.domain.storage.infrastructure.jpa;

import io.lette1394.mediaserver.domain.storage.object.BinarySupplier;
import io.lette1394.mediaserver.domain.storage.object.Identifier;
import io.lette1394.mediaserver.domain.storage.object.Object;
import io.lette1394.mediaserver.domain.storage.object.Storage;
import io.lette1394.mediaserver.domain.storage.usecase.ObjectNotFoundException;
import java.util.concurrent.CompletableFuture;

class DatabaseStorage implements Storage {
  @Override
  public CompletableFuture<Boolean> isExist(Identifier identifier) throws ObjectNotFoundException {
    return null;
  }

  @Override
  public CompletableFuture<Object> find(Identifier identifier) throws ObjectNotFoundException {
    return null;
  }

  @Override
  public CompletableFuture<BinarySupplier> findBinary(Object object) {
    return null;
  }

  @Override
  public CompletableFuture<Void> create(Object object, BinarySupplier binarySupplier) {
    return null;
  }

  @Override
  public CompletableFuture<Void> append(Object object, BinarySupplier binarySupplier) {
    return null;
  }
}
