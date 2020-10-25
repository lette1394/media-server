package io.lette1394.mediaserver.storage.domain.object;

import io.lette1394.mediaserver.storage.domain.binary.BinaryRepository;
import java.time.OffsetDateTime;

public class Factory {
  private final BinaryRepository binaryRepository;
  private final Policy policy;

  public Factory(BinaryRepository binaryRepository,
    Policy policy) {
    this.binaryRepository = binaryRepository;
    this.policy = policy;
  }

  public Factory(BinaryRepository binaryRepository) {
    this(binaryRepository, Policy.ALL_POLICY);
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
      .policy(policy)
      .binaryRepository(binaryRepository)
      .attributes(attributes)
      .build();
  }
}
