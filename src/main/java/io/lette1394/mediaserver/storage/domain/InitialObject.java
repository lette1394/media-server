package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.Result;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Builder;

public class InitialObject extends Object {
  private final AtomicLong processingSize;

  @Builder
  public InitialObject(Identifier identifier,
    Attributes attributes,
    BinaryRepository binaryRepository,
    ObjectPolicy objectPolicy) {
    super(identifier, attributes, binaryRepository, objectPolicy);
    this.processingSize = new AtomicLong(0L);
  }

  @Override
  protected CompletableFuture<Result<Void>> upload0(BinarySupplier binarySupplier) {
    return binaryRepository.createBinary(
      identifier,
      new AccumulatingSizeBinarySupplier(
        binarySupplier,
        processingSize::set));
  }

  @Override
  protected ObjectState getObjectState() {
    return ObjectState.INITIAL;
  }

  @Override
  public long getSize() {
    return 0;
  }

  @Override
  public long getProgressingSize() {
    return processingSize.get();
  }
}
