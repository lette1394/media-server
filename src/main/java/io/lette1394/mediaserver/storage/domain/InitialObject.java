package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.Result;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;

public class InitialObject extends Object {
  @Builder
  public InitialObject(Identifier identifier,
    Attributes attributes,
    BinaryRepository binaryRepository,
    ObjectPolicy objectPolicy) {
    super(identifier, attributes, binaryRepository, objectPolicy);
  }

  @Override
  protected CompletableFuture<Result<Void>> upload0(BinarySupplier binarySupplier) {
    return binaryRepository.createBinary(identifier, binarySupplier);
  }

  @Override
  protected ObjectState getObjectState() {
    return ObjectState.INITIAL;
  }

  @Override
  public long getSize() {
    return 0;
  }
}
