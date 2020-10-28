package io.lette1394.mediaserver.storage2.domain;

import java.util.concurrent.CompletableFuture;

public interface ObjectRepository<BUFFER extends SizeAware> {

  CompletableFuture<Boolean> exists(ObjectPath objectPath);

  CompletableFuture<Object<BUFFER>> find(ObjectPath objectPath) throws ObjectNotFoundException;

  CompletableFuture<Object<BUFFER>> save(Object<BUFFER> object);

  CompletableFuture<Void> delete(ObjectPath objectPath);
}
