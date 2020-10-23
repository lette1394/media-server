package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.PositiveLong;
import io.lette1394.mediaserver.common.Result;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;

public class FulfilledObject extends Object {
  private final PositiveLong size;

  @Builder
  public FulfilledObject(Identifier identifier,
    Attributes attributes,
    BinaryRepository binaryRepository,
    ObjectPolicy objectPolicy, PositiveLong size) {
    super(identifier, attributes, binaryRepository, objectPolicy);
    this.size = size;
  }

  @Override
  public CompletableFuture<Result<Void>> upload0(BinarySupplier binarySupplier) {
    return binaryRepository.createBinary(identifier, binarySupplier);
  }

  @Override
  protected ObjectState getObjectState() {
    return ObjectState.FULFILLED;
  }

  // TODO: Size object
  @Override
  public long getSize() {
    return size.get();
  }
}
