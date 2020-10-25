package io.lette1394.mediaserver.storage.domain.object;

import io.lette1394.mediaserver.storage.domain.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.object.Attributes;
import io.lette1394.mediaserver.storage.domain.object.Identifier;
import io.lette1394.mediaserver.storage.domain.object.InitialObject;
import io.lette1394.mediaserver.storage.domain.object.Object;
import io.lette1394.mediaserver.storage.domain.object.Policy;
import io.lette1394.mediaserver.storage.domain.object.Tag;
import io.lette1394.mediaserver.storage.domain.object.Tags;
import java.time.OffsetDateTime;

public class ObjectFactory {
  private final BinaryRepository binaryRepository;
  private final Policy policy;

  public ObjectFactory(BinaryRepository binaryRepository,
    Policy policy) {
    this.binaryRepository = binaryRepository;
    this.policy = policy;
  }

  public ObjectFactory(BinaryRepository binaryRepository) {
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
