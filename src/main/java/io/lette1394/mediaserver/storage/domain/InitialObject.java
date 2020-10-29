package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.TimeStamp;
import lombok.Builder;

public class InitialObject<BUFFER extends SizeAware> extends Object<BUFFER> {

  @Builder
  public InitialObject(Identifier identifier,
    ObjectPolicy objectPolicy, BinaryPolicy binaryPolicy,
    Tags tags, TimeStamp timeStamp,
    BinarySnapshot binarySnapshot,
    BinarySupplier<BUFFER> binarySupplier) {
    super(identifier, objectPolicy, binaryPolicy, tags, timeStamp, binarySnapshot, binarySupplier);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.INITIAL;
  }

  @Override
  public long getSize() {
    return 0L;
  }
}
