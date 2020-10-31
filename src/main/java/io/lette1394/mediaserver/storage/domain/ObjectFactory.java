package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.TimeStamp;
import java.time.OffsetDateTime;

public class ObjectFactory<BUFFER extends SizeAware> {

  ObjectPolicy objectPolicy;
  BinaryPolicy binaryPolicy;

  public ObjectFactory(ObjectPolicy objectPolicy,
    BinaryPolicy binaryPolicy) {
    this.objectPolicy = objectPolicy;
    this.binaryPolicy = binaryPolicy;
  }

  public ObjectFactory() {
    this(ObjectPolicy.ALL_OBJECT_POLICY, BinaryPolicy.ALL_BINARY_POLICY);
  }

  public <T> Object<BUFFER> create(Identifier identifier) {
    return create(identifier.getArea(), identifier.getKey());
  }

  public <T> Object<BUFFER> create(String area, String key) {
    final Identifier identifier = new Identifier(area, key);

    return Object.<BUFFER>builder()
      .identifier(identifier)
      .objectPolicy(objectPolicy)
      .objectSnapshot(ObjectSnapshot.initial(0))
      .binaryPolicy(binaryPolicy)
      .binarySnapshot(BinarySnapshot.initial())
      .tags(Tags.empty())
      .timeStamp(TimeStamp.builder()
        .created(OffsetDateTime.now())
        .updated(OffsetDateTime.now())
        .build())
      .build();
  }
}
