package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.PositiveOrZeroLongAdder;
import io.lette1394.mediaserver.common.Result;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;

public class InitialObject extends Object {
  private final PositiveOrZeroLongAdder size;

  @Builder
  public InitialObject(Identifier identifier,
    Attributes attributes,
    BinaryRepository binaryRepository,
    ObjectPolicy objectPolicy) {
    super(identifier, attributes, binaryRepository, objectPolicy);
    this.size = new PositiveOrZeroLongAdder(0L);
  }

  @Override
  protected CompletableFuture<Result<Void>> upload0(BinarySupplier binarySupplier) {
    return binaryRepository.createBinary(
      identifier,
      new AccumulatingSizeBinarySupplier(
        binarySupplier,
        size::add));
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
    return size.get();
  }
}
