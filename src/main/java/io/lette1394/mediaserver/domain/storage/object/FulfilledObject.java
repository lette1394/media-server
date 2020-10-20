package io.lette1394.mediaserver.domain.storage.object;

import java.util.concurrent.CompletableFuture;
import lombok.Builder;

public class FulfilledObject extends Object {
  @Builder
  public FulfilledObject(Identifier identifier,
    Attributes attributes,
    BinaryRepository binaryRepository,
    ObjectLifecyclePolicy objectLifecyclePolicy) {
    super(identifier, attributes, binaryRepository, objectLifecyclePolicy);
  }

  @Override
  public CompletableFuture<Void> upload0(BinarySupplier binarySupplier) {
    return binaryRepository.createBinary(this, binarySupplier);
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
