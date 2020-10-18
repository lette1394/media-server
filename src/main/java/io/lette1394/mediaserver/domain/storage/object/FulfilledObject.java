package io.lette1394.mediaserver.domain.storage.object;

import java.util.concurrent.CompletableFuture;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class FulfilledObject extends Object {
  @Override
  public CompletableFuture<Void> upload0(BinarySupplier binarySupplier) {
    return storage.create(this, binarySupplier);
  }

  @Override
  public boolean isInitial() {
    return false;
  }

  @Override
  public boolean isPending() {
    return false;
  }

  @Override
  public boolean isFulfilled() {
    return true;
  }
}
