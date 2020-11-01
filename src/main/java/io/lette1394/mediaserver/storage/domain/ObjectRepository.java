package io.lette1394.mediaserver.storage.domain;

import java.util.concurrent.CompletableFuture;

public interface ObjectRepository<BUFFER extends Payload> {
  CompletableFuture<Boolean> exists(Identifier identifier);

  CompletableFuture<Object<BUFFER>> find(Identifier identifier) throws ObjectNotFoundException;

  CompletableFuture<Object<BUFFER>> save(Object<BUFFER> object);

  CompletableFuture<Void> delete(Identifier identifier);
}
