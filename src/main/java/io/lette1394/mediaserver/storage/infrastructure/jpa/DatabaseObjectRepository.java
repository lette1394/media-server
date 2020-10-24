package io.lette1394.mediaserver.storage.infrastructure.jpa;

import io.lette1394.mediaserver.storage.domain.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectRepository;
import io.lette1394.mediaserver.storage.infrastructure.jpa.DatabaseStorageObjectEntity.ObjectId;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class DatabaseObjectRepository implements ObjectRepository {
  private final DatabaseObjectEntityRepository repository;
  private final BinaryRepository binaryRepository;

  @Override
  public CompletableFuture<Boolean> objectExists(Identifier identifier) {
    return CompletableFuture
      .completedFuture(repository.existsById(new ObjectId(identifier)));
  }

  @Override
  public CompletableFuture<Object> findObject(Identifier identifier) {
    return repository
      .findById(new ObjectId(identifier))
      .map(entity -> entity.toObject(binaryRepository))
      .map(CompletableFuture::completedFuture)
      .orElseGet(() -> CompletableFuture.failedFuture(new RuntimeException()));
  }

  @Override
  public CompletableFuture<Object> saveObject(Object object) {
    return null;
  }
}
