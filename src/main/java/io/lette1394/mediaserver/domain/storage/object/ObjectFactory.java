package io.lette1394.mediaserver.domain.storage.object;

import io.lette1394.mediaserver.common.PositiveLong;
import java.time.OffsetDateTime;
import lombok.Value;

@Value
public class ObjectFactory {
  Storage storage;

  public Object create(String area, String key) {
    return Object.builder()
        .storage(storage)
        .identifier(new Identifier(area, key))
        .attributes(
            Attributes.builder()
                .tags(Tags.tags(Tag.tag("k1", "v1"), Tag.tag("k2", "v2")))
                .size(PositiveLong.positiveLong(123))
                .type(Type.FILE)
                .created(OffsetDateTime.now())
                .updated(OffsetDateTime.now())
                .build())
        .objectUploadPolicy(object -> true)
        .build();
  }
}
