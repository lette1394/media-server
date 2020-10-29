package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.PositiveLong;
import io.lette1394.mediaserver.common.TimeStamp;
import lombok.Builder;

public class FulfilledObject<BUFFER extends SizeAware> extends Object<BUFFER> {

  private final PositiveLong size;

  @Builder
  public FulfilledObject(Identifier identifier,
    ObjectPolicy objectPolicy, BinaryPolicy binaryPolicy,
    Tags tags, TimeStamp timeStamp,
    BinarySnapshot binarySnapshot,
    BinarySupplier<BUFFER> binarySupplier, PositiveLong size) {
    super(identifier, objectPolicy, binaryPolicy, tags, timeStamp, binarySnapshot, binarySupplier);
    this.size = size;
  }

  @Override
  public long getSize() {
    return size.get();
  }

  @Override
  public ObjectType getType() {
    return ObjectType.FULFILLED;
  }
}
