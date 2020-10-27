package io.lette1394.mediaserver.storage.domain.object;

import io.lette1394.mediaserver.common.PositiveLong;
import io.lette1394.mediaserver.common.TimeStamp;
import io.lette1394.mediaserver.storage.domain.binary.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.binary.BinarySupplier;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;

public class FulfilledObject extends Object {
  private final PositiveLong size;

  @Builder
  public FulfilledObject(Identifier identifier,
    BinaryRepository binaryRepository, Policy policy,
    TimeStamp timeStamp,
    Tags tags, PositiveLong size) {
    super(identifier, binaryRepository, policy, timeStamp, tags);
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
