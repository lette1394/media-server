package io.lette1394.mediaserver.storage.domain;

import java.util.concurrent.CompletableFuture;

public interface ObjectRepository<BUFFER extends Payload> {
  CompletableFuture<Boolean> objectExists(Identifier identifier);

  CompletableFuture<Object<BUFFER>> findObject(Identifier identifier);

  CompletableFuture<Object<BUFFER>> saveObject(Object<BUFFER> object);

  CompletableFuture<Void> deleteObject(Identifier identifier);
}
