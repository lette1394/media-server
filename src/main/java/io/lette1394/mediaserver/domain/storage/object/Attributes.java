package io.lette1394.mediaserver.domain.storage.object;

import io.lette1394.mediaserver.common.PositiveLong;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Attributes {
  PositiveLong size;

  Tags tags;

  OffsetDateTime created;
  OffsetDateTime updated;
}
