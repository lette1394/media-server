package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.PositiveLong;
import io.lette1394.mediaserver.common.Result;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;


public class PendingObject extends Object {
  private final PositiveLong size;

  @Builder
  public PendingObject(Identifier identifier,
    Attributes attributes,
    BinaryRepository binaryRepository,
    ObjectPolicy objectPolicy, PositiveLong size) {
    super(identifier, attributes, binaryRepository, objectPolicy);
    this.size = size;
  }

  @Override
  public CompletableFuture<Result<Void>> upload0(BinarySupplier binarySupplier) {
    return binaryRepository.appendBinary(identifier, binarySupplier);
  }

  @Override
  protected ObjectState getObjectState() {
    return ObjectState.PENDING;
  }

  @Override
  public long getSize() {
    return size.getValue();
  }
}
