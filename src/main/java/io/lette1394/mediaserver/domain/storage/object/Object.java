package io.lette1394.mediaserver.domain.storage.object;

import io.lette1394.mediaserver.common.PositiveLong;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder(access = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class Object {
  @Getter private final Identifier identifier;
  private final Attributes attributes;

  public static Object create(String area, String key) {
    return builder()
        .identifier(new Identifier(area, key))
        .attributes(
            Attributes.builder()
                .tags(Tags.tags(Tag.tag("k1", "v1"), Tag.tag("k2", "v2")))
                .size(PositiveLong.positiveLong(123))
                .type(Type.FILE)
                .created(OffsetDateTime.now())
                .updated(OffsetDateTime.now())
                .build())
        .build();
  }
}
