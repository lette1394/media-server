package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.PositiveLong;
import io.lette1394.mediaserver.common.Result;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Builder;

public class PendingObject extends Object {
  private final PositiveLong size;
  private final AtomicLong progressingSize;

  @Builder
  public PendingObject(Identifier identifier,
    Attributes attributes,
    BinaryRepository binaryRepository,
    ObjectPolicy objectPolicy, PositiveLong size) {
    super(identifier, attributes, binaryRepository, objectPolicy);
    this.size = size;
    this.progressingSize = new AtomicLong(size.get());
  }

  @Override
  public CompletableFuture<Result<Void>> upload0(BinarySupplier binarySupplier) {
    return binaryRepository.appendBinary(
      identifier,
      new AccumulatingSizeBinarySupplier(
        binarySupplier,
        progressingSize::set));
  }

  @Override
  protected ObjectState getObjectState() {
    return ObjectState.PENDING;
  }

  @Override
  public long getSize() {
    return size.get();
  }
}
