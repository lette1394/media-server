package io.lette1394.mediaserver.storage.domain.object;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Attributes {
  // TODO: 이거 필요한가?

  Tags tags;

  OffsetDateTime created;
  OffsetDateTime updated;
}
