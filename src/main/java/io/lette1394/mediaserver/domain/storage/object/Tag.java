package io.lette1394.mediaserver.domain.storage.object;

import static io.lette1394.mediaserver.common.NonBlankString.nonBlankString;

import io.lette1394.mediaserver.common.NonBlankString;
import lombok.Value;

@Value
class Tag {
  NonBlankString key;
  NonBlankString value;

  public static Tag tag(String key, String value) {
    return new Tag(nonBlankString(key), nonBlankString(value));
  }
}
