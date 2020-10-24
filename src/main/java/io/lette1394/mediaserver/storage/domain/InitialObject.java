package io.lette1394.mediaserver.storage.domain;

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
  protected CompletableFuture<Void> upload0(BinarySupplier binarySupplier) {
    return binaryRepository.saveBinary(identifier, binarySupplier);
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
