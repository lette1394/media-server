package io.lette1394.mediaserver.storage.domain.object;

import io.lette1394.mediaserver.common.TimeStamp;
import java.time.OffsetDateTime;
import lombok.Value;

@Value
public class ObjectFactory {

  Policy policy;

  public Object create(String area, String key) {
    final Identifier identifier = new Identifier(area, key);

    return InitialObject.builder()
      .identifier(identifier)
      .policy(policy)
      .tags(Tags.empty())
      .timeStamp(TimeStamp.builder()
        .created(OffsetDateTime.now())
        .updated(OffsetDateTime.now())
        .build())
      .build();
  }
}
