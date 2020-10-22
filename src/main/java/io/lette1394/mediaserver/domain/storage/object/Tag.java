package io.lette1394.mediaserver.domain.storage.object;

import static io.lette1394.mediaserver.common.NonBlankString.nonBlankString;

import io.lette1394.mediaserver.common.NonBlankString;
import lombok.Value;

@Value
public class Tag {
  NonBlankString key;
  String value;

  public static Tag tag(String key, String value) {
    return new Tag(nonBlankString(key), value);
  }
}
