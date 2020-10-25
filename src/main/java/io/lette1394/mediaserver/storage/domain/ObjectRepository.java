package io.lette1394.mediaserver.storage.domain;

import java.util.concurrent.CompletableFuture;

public interface ObjectRepository {
  CompletableFuture<Boolean> objectExists(Identifier identifier);

  CompletableFuture<Object> findObject(Identifier identifier);

  CompletableFuture<Object> saveObject(Object object);

  CompletableFuture<Void> deleteObject(Identifier identifier);
}
