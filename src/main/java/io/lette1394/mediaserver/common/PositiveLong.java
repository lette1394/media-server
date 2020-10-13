package io.lette1394.mediaserver.common;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(staticName = "positiveLong")
public class PositiveLong {
  long value;
}
