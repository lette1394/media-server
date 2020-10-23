package io.lette1394.mediaserver.storage.infrastructure.jpa;

import static java.util.concurrent.CompletableFuture.completedFuture;

import io.lette1394.mediaserver.common.Result;
import io.lette1394.mediaserver.storage.infrastructure.jpa.DatabaseStorageObjectEntity.ObjectId;
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
  private final DatabaseObjectEntityRepository repository;
  private final BinaryRepository binaryRepository;

  @Override
  public CompletableFuture<Result<Boolean>> objectExists(Identifier identifier) {
    return completedFuture(Result.succeed(repository.existsById(new ObjectId(identifier))));
  }

  @Override
  public CompletableFuture<Result<Object>> findObject(Identifier identifier) {
    return repository
      .findById(new ObjectId(identifier))
      .map(entity -> entity.toObject(binaryRepository))
      .map(Result::succeed)
      .map(CompletableFuture::completedFuture)
      .orElseGet(() -> CompletableFuture.failedFuture(new RuntimeException()));
  }

  @Override
  public CompletableFuture<Result<Void>> createObject(Object object) {
    return null;
  }
}
