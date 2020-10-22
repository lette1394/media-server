package io.lette1394.mediaserver.domain.storage.object;

import io.lette1394.mediaserver.common.PositiveLong;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;

public class FulfilledObject extends Object implements SizeAware {
  private final PositiveLong size;

  @Builder
  public FulfilledObject(Identifier identifier,
    Attributes attributes,
    BinaryRepository binaryRepository,
    ObjectLifecyclePolicy objectLifecyclePolicy, PositiveLong size) {
    super(identifier, attributes, binaryRepository, objectLifecyclePolicy);
    this.size = size;
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


  // TODO: Size object
  @Override
  public long getSize() {
    return size.getValue();
  }
}
