package io.lette1394.mediaserver.domain.storage.object;

import io.lette1394.mediaserver.domain.storage.usecase.ObjectNotFoundException;
import java.util.concurrent.CompletableFuture;

public interface ObjectRepository {
  CompletableFuture<Boolean> doesObjectExist(Identifier identifier) throws ObjectNotFoundException;

  CompletableFuture<Object> findObject(Identifier identifier) throws ObjectNotFoundException;
}
