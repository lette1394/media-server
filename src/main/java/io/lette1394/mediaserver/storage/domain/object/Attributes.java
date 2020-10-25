package io.lette1394.mediaserver.storage.domain.object;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Attributes {
  Tags tags;

  OffsetDateTime created;
  OffsetDateTime updated;
}
