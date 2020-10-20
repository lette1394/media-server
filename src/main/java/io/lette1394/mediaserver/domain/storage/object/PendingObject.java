package io.lette1394.mediaserver.domain.storage.object;

import java.util.concurrent.CompletableFuture;
import lombok.Builder;


public class PendingObject extends Object {

  @Builder
  public PendingObject(Identifier identifier,
    Attributes attributes,
    BinaryRepository binaryRepository,
    ObjectLifecyclePolicy objectLifecyclePolicy) {
    super(identifier, attributes, binaryRepository, objectLifecyclePolicy);
  }

  @Override
  public CompletableFuture<Void> upload0(BinarySupplier binarySupplier) {
    return binaryRepository.appendBinary(this, binarySupplier);
  }

  @Override
  public boolean isInitial() {
    return false;
  }

  @Override
  public boolean isPending() {
    return true;
  }

  @Override
  public boolean isFulfilled() {
    return false;
  }
}
