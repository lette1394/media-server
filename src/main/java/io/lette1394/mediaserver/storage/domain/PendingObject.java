package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.PositiveLong;
import io.lette1394.mediaserver.common.PositiveOrZeroLongAdder;
import io.lette1394.mediaserver.common.Result;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;

public class PendingObject extends Object {
  private final PositiveLong size;
  private final PositiveOrZeroLongAdder progressingSize;

  @Builder
  public PendingObject(Identifier identifier,
    Attributes attributes,
    BinaryRepository binaryRepository,
    ObjectPolicy objectPolicy, PositiveLong size) {
    super(identifier, attributes, binaryRepository, objectPolicy);
    this.size = size;
    this.progressingSize = new PositiveOrZeroLongAdder(size);
  }

  @Override
  public CompletableFuture<Result<Void>> upload0(BinarySupplier binarySupplier) {
    return binaryRepository.appendBinary(
      identifier,
      new AccumulatingSizeBinarySupplier(
        binarySupplier,
        progressingSize::add));
  }

  @Override
  protected ObjectState getObjectState() {
    return ObjectState.PENDING;
  }

  @Override
  public long getSize() {
    return size.get();
  }

  @Override
  public long getProgressingSize() {
    return progressingSize.get();
  }
}
