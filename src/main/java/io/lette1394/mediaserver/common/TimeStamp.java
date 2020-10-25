package io.lette1394.mediaserver.common;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Value;

@Value
public class TimeStamp {
  OffsetDateTime created;
  OffsetDateTime updated;

  @Builder
  public TimeStamp(OffsetDateTime created, OffsetDateTime updated) {
    this.created = created;
    this.updated = updated;
  }
}
