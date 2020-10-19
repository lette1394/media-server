package io.lette1394.mediaserver.domain.storage.infrastructure;

import io.lette1394.mediaserver.domain.storage.object.BinarySupplier;
import io.lette1394.mediaserver.domain.storage.object.Identifier;
import io.lette1394.mediaserver.domain.storage.object.Object;
import io.lette1394.mediaserver.domain.storage.object.Storage;
import io.lette1394.mediaserver.domain.storage.usecase.ObjectNotFoundException;
import java.util.concurrent.CompletableFuture;

public class AwsS3Storage implements Storage {


  @Override
  public CompletableFuture<Boolean> objectExists(Identifier identifier) throws ObjectNotFoundException {
    return null;
  }

  @Override
  public CompletableFuture<Object> findObject(Identifier identifier) throws ObjectNotFoundException {
    return null;
  }

  @Override
  public CompletableFuture<BinarySupplier> findBinary(Object object) {
    return null;
  }

  @Override
  public CompletableFuture<Void> createBinary(Object object, BinarySupplier binarySupplier) {
    return null;
  }

  @Override
  public CompletableFuture<Void> appendBinary(Object object, BinarySupplier binarySupplier) {
    return null;
  }
}
