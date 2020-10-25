package io.lette1394.mediaserver.storage.infrastructure.jpa;

import io.lette1394.mediaserver.storage.domain.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.object.Identifier;
import io.lette1394.mediaserver.storage.domain.object.Object;
import io.lette1394.mediaserver.storage.domain.object.ObjectRepository;
import io.lette1394.mediaserver.storage.infrastructure.jpa.DatabaseStorageObjectEntity.ObjectId;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class DatabaseObjectRepository implements ObjectRepository {
  private final DatabaseObjectEntityRepository db;
  private final BinaryRepository binaryRepository;

  @Override
  public CompletableFuture<Boolean> objectExists(Identifier identifier) {
    return CompletableFuture
      .completedFuture(db.existsById(new ObjectId(identifier)));
  }

  @Override
  public CompletableFuture<Object> findObject(Identifier identifier) {
    return db
      .findById(new ObjectId(identifier))
      .map(entity -> entity.toObject(binaryRepository))
      .map(CompletableFuture::completedFuture)
      .orElseGet(() -> CompletableFuture.failedFuture(new RuntimeException()));
  }

  @Override
  public CompletableFuture<Object> saveObject(Object object) {
    db.save(DatabaseStorageObjectEntity.fromObject(object));
    return null;
  }

  @Override
  public CompletableFuture<Void> deleteObject(Identifier identifier) {
    return null;
  }
}
