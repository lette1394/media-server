package io.lette1394.mediaserver.domain.storage;

import io.lette1394.mediaserver.common.PositiveLong;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
class Attributes {
    Type type;
    PositiveLong size;

    Tags tags;

    OffsetDateTime created;
    OffsetDateTime updated;
}
