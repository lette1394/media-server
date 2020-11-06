package io.lette1394.mediaserver.storage.domain;

import java.util.concurrent.CompletableFuture;


// TODO: [COPY]여기서 copy link 를 follow 하는 object를 생성할 수 있는 layer를 껴 넣을 수 있을 거 같다.
//
public interface ObjectRepository<BUFFER extends Payload> {
  CompletableFuture<Boolean> exists(Identifier identifier);

  CompletableFuture<Object<BUFFER>> find(Identifier identifier) throws ObjectNotFoundException;

  CompletableFuture<Object<BUFFER>> save(Object<BUFFER> object);

  CompletableFuture<Void> delete(Identifier identifier);
}
