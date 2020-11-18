package io.lette1394.mediaserver.storage.domain;

import java.util.concurrent.CompletableFuture;

public interface ObjectRepository<P extends Payload> {
  CompletableFuture<Boolean> exists(Identifier identifier);

  CompletableFuture<Object<P>> find(Identifier identifier) throws ObjectNotFoundException;

  CompletableFuture<Object<P>> save(Object<P> object);

  CompletableFuture<Void> delete(Identifier identifier);
}
