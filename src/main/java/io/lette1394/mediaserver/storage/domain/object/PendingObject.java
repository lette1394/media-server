package io.lette1394.mediaserver.storage.domain.object;

import io.lette1394.mediaserver.common.PositiveLong;
import io.lette1394.mediaserver.storage.domain.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.object.Attributes;
import io.lette1394.mediaserver.storage.domain.object.Identifier;
import io.lette1394.mediaserver.storage.domain.object.Object;
import io.lette1394.mediaserver.storage.domain.object.Policy;
import io.lette1394.mediaserver.storage.domain.object.State;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;

public class PendingObject extends Object {
  private final PositiveLong size;

  @Builder
  public PendingObject(Identifier identifier,
    Attributes attributes,
    BinaryRepository binaryRepository,
    Policy policy, PositiveLong size) {
    super(identifier, attributes, binaryRepository, policy);
    this.size = size;
  }

  @Override
  public CompletableFuture<Void> upload0(BinarySupplier binarySupplier) {
    return binaryRepository.appendBinary(identifier, binarySupplier);
  }

  @Override
  protected State getObjectState() {
    return State.PENDING;
  }

  @Override
  public long getSize() {
    return size.get();
  }
}
