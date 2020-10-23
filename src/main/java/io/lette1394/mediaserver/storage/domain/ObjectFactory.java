package io.lette1394.mediaserver.storage.domain;

import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ObjectFactory {
  private final BinaryRepository binaryRepository;
  private final ObjectPolicy objectPolicy = ObjectPolicy.ALL_POLICY;

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
