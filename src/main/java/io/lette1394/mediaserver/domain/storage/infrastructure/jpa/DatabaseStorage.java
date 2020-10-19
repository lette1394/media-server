package io.lette1394.mediaserver.domain.storage.infrastructure.jpa;

import static java.util.concurrent.CompletableFuture.completedFuture;

import io.lette1394.mediaserver.domain.storage.infrastructure.jpa.DatabaseStorageObjectEntity.ObjectId;
import io.lette1394.mediaserver.domain.storage.object.BinarySupplier;
import io.lette1394.mediaserver.domain.storage.object.Identifier;
import io.lette1394.mediaserver.domain.storage.object.Object;
import io.lette1394.mediaserver.domain.storage.object.ObjectFactory;
import io.lette1394.mediaserver.domain.storage.object.Storage;
import io.lette1394.mediaserver.domain.storage.usecase.ObjectNotFoundException;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class DatabaseStorage implements Storage {
  private final DatabaseObjectEntityRepository repository;

  @Override
  public CompletableFuture<Boolean> doesObjectExist(Identifier identifier) throws ObjectNotFoundException {
    return completedFuture(repository.existsById(new ObjectId(identifier)));
  }

  @Override
  public CompletableFuture<Object> findObject(Identifier identifier) throws ObjectNotFoundException {
    return repository
      .findById(new ObjectId(identifier))
      .map(entity -> entity.toObject(this))
      .map(CompletableFuture::completedFuture)
      .orElseGet(() -> CompletableFuture.failedFuture(new RuntimeException()));
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
