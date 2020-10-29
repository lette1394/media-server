package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.TimeStamp;
import java.time.OffsetDateTime;
import lombok.Value;

@Value
public class ObjectFactory<BUFFER extends SizeAware> {

  ObjectPolicy objectPolicy;

  public Object<BUFFER> create(String area, String key) {
    final Identifier identifier = new Identifier(area, key);

    return InitialObject.<BUFFER>builder()
      .identifier(identifier)
      .objectPolicy(objectPolicy)
      .tags(Tags.empty())
      .timeStamp(TimeStamp.builder()
        .created(OffsetDateTime.now())
        .updated(OffsetDateTime.now())
        .build())
      .build();
  }
}
