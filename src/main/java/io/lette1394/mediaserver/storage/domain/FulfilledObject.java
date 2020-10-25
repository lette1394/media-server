package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.PositiveLong;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;

public class FulfilledObject extends Object {
  private final PositiveLong size;

  @Builder
  public FulfilledObject(Identifier identifier,
    Attributes attributes,
    BinaryRepository binaryRepository,
    Policy policy, PositiveLong size) {
    super(identifier, attributes, binaryRepository, policy);
    this.size = size;
  }

  @Override
  public CompletableFuture<Void> upload0(BinarySupplier binarySupplier) {
    return binaryRepository.saveBinary(identifier, binarySupplier);
  }

  @Override
  protected State getObjectState() {
    return State.FULFILLED;
  }

  // TODO: Size object
  @Override
  public long getSize() {
    return size.get();
  }
}
