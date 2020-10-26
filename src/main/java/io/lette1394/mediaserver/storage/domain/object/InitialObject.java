package io.lette1394.mediaserver.storage.domain.object;

import io.lette1394.mediaserver.common.TimeStamp;
import io.lette1394.mediaserver.storage.domain.binary.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.binary.BinarySupplier;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;

public class InitialObject extends Object {

  @Builder
  public InitialObject(Identifier identifier,
    BinaryRepository<? super BinarySupplier> binaryRepository, Policy policy,
    TimeStamp timeStamp,
    Tags tags) {
    super(identifier, binaryRepository, policy, timeStamp, tags);
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
