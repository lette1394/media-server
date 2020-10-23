package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.Result;
import java.util.concurrent.CompletableFuture;

public interface ObjectRepository {
  CompletableFuture<Result<Boolean>> objectExists(Identifier identifier);

  CompletableFuture<Result<Object>> findObject(Identifier identifier);

  CompletableFuture<Result<Void>> createObject(Object object);
}
