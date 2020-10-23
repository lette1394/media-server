package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.PositiveLong;
import io.lette1394.mediaserver.common.PositiveOrZeroLongAdder;
import io.lette1394.mediaserver.common.Result;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Builder;

public class FulfilledObject extends Object {
  private final PositiveLong size;
  private final AtomicLong progressingSize;

  @Builder
  public FulfilledObject(Identifier identifier,
    Attributes attributes,
    BinaryRepository binaryRepository,
    ObjectPolicy objectPolicy, PositiveLong size) {
    super(identifier, attributes, binaryRepository, objectPolicy);
    this.size = size;
    this.progressingSize = new AtomicLong(0L);
  }

  @Override
  public CompletableFuture<Result<Void>> upload0(BinarySupplier binarySupplier) {
    return binaryRepository
      .createBinary(
        identifier,
        new AccumulatingSizeBinarySupplier(
          binarySupplier,
          progressingSize::set));
  }

  @Override
  protected ObjectState getObjectState() {
    return ObjectState.FULFILLED;
  }

  // TODO: Size object
  @Override
  public long getSize() {
    return size.get();
  }

  @Override
  public long getProgressingSize() {
    return progressingSize.get();
  }
}
