package io.lette1394.mediaserver.storage.domain.object;

import io.lette1394.mediaserver.storage.domain.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.object.Attributes;
import io.lette1394.mediaserver.storage.domain.object.Identifier;
import io.lette1394.mediaserver.storage.domain.object.Object;
import io.lette1394.mediaserver.storage.domain.object.Policy;
import io.lette1394.mediaserver.storage.domain.object.State;
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
