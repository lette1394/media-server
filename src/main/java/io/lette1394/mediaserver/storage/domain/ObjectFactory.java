package io.lette1394.mediaserver.storage.domain;

import java.time.OffsetDateTime;

public class ObjectFactory {
  private final BinaryRepository binaryRepository;
  private final ObjectPolicy objectPolicy;

  public ObjectFactory(BinaryRepository binaryRepository,
    ObjectPolicy objectPolicy) {
    this.binaryRepository = binaryRepository;
    this.objectPolicy = objectPolicy;
  }

  public ObjectFactory(BinaryRepository binaryRepository) {
    this(binaryRepository, ObjectPolicy.ALL_POLICY);
  }

  public Object create(String area, String key) {
    final Identifier identifier = new Identifier(area, key);

    final Attributes attributes = Attributes.builder()
      .tags(Tags.tags(Tag.tag("k1", "v1"), Tag.tag("k2", "v2")))
      .created(OffsetDateTime.now())
      .updated(OffsetDateTime.now())
      .build();

    return InitialObject.builder()
      .identifier(identifier)
      .objectPolicy(objectPolicy)
      .binaryRepository(binaryRepository)
      .attributes(attributes)
      .build();
  }
}
