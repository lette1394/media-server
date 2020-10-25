package io.lette1394.mediaserver.storage.domain.object;

import io.lette1394.mediaserver.storage.domain.binary.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.binary.BinarySupplier;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;

public class InitialObject extends Object {
  @Builder
  public InitialObject(Identifier identifier,
    Attributes attributes,
    BinaryRepository binaryRepository,
    Policy policy) {
    super(identifier, attributes, binaryRepository, policy);
  }

  @Override
  protected CompletableFuture<Void> upload0(BinarySupplier binarySupplier) {
    return binaryRepository.saveBinary(identifier, binarySupplier);
  }

  @Override
  protected State getObjectState() {
    return State.INITIAL;
  }

  @Override
  public long getSize() {
    return 0;
  }
}
