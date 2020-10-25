package io.lette1394.mediaserver.storage.domain.object;

import io.lette1394.mediaserver.common.PositiveLong;
import io.lette1394.mediaserver.storage.domain.binary.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.binary.BinarySupplier;
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
