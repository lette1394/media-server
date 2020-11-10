package io.lette1394.mediaserver.storage.domain;

import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DelegatingObjectRepository<B extends Payload> implements ObjectRepository<B> {
  protected final ObjectRepository<B> delegate;

  @Override
  public CompletableFuture<Boolean> exists(Identifier identifier) {
    return delegate.exists(identifier);
  }

  @Override
  public CompletableFuture<Object<B>> find(Identifier identifier) throws ObjectNotFoundException {
    return delegate.find(identifier);
  }

  @Override
  public CompletableFuture<Object<B>> save(Object<B> object) {
    return delegate.save(object);
  }

  @Override
  public CompletableFuture<Void> delete(Identifier identifier) {
    return delegate.delete(identifier);
  }
}
