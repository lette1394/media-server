package io.lette1394.mediaserver.domain.storage.object;

import io.lette1394.mediaserver.common.NonBlankString;
import lombok.Value;

@Value
public class Identifier {
  NonBlankString area;
  NonBlankString key;

  public Identifier(String area, String key) {
    this.area = NonBlankString.nonBlankString(area);
    this.key = NonBlankString.nonBlankString(key);
  }

  public String getArea() {
    return area.getValue();
  }

  public String getKey() {
    return key.getValue();
  }
}
