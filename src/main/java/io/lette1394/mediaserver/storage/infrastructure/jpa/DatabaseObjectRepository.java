package io.lette1394.mediaserver.storage.infrastructure.jpa;

import static io.lette1394.mediaserver.storage.infrastructure.jpa.DatabaseStorageObjectEntity.ObjectId.fromIdentifier;
import static io.lette1394.mediaserver.storage.infrastructure.jpa.DatabaseStorageObjectEntity.fromObject;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;

import io.lette1394.mediaserver.storage.domain.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectRepository;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class DatabaseObjectRepository implements ObjectRepository {
  private final DatabaseObjectEntityRepository db;
  private final BinaryRepository binaryRepository;

  @Override
  public CompletableFuture<Boolean> exists(Identifier identifier) {
    return completedFuture(db.existsById(fromIdentifier(identifier)));
  }

  @Override
  public CompletableFuture<Object> find(Identifier identifier) {
    return db
      .findById(fromIdentifier(identifier))
      .map(entity -> entity.toObject(binaryRepository))
      .map(CompletableFuture::completedFuture)
      .orElseGet(() -> CompletableFuture.failedFuture(new RuntimeException("unhandled")));
  }

  @Override
  public CompletableFuture<Object> save(Object object) {
    final DatabaseStorageObjectEntity saved = db.save(fromObject(object));
    return completedFuture(saved.toObject(binaryRepository));
  }

  @Override
  public CompletableFuture<Void> delete(Identifier identifier) {
    try {
      db.deleteById(fromIdentifier(identifier));
      return completedFuture(null);
    } catch (Exception e) {
      return failedFuture(e);
    }
  }
}
