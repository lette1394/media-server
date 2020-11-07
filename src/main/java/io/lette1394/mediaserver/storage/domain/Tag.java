package io.lette1394.mediaserver.storage.domain;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import io.lette1394.mediaserver.common.Contracts;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@EqualsAndHashCode
public class Tag {
  private final String key;
  private final String value;

  public Tag(String key) {
    this(key, null);
  }

  public Tag(String key, String value) {
    Contracts.require(isNotBlank(key), "Tag key cannot be blank");
    this.key = key;
    this.value = value;
  }

  public static Tag tag(String key, String value) {
    return new Tag(key, value);
  }

  public long asLong() {
    return Long.parseLong(value);
  }

  public long asLongOrDefault(long defaultValue) {
    if (StringUtils.isBlank(value)) {
      return defaultValue;
    }
    try {
      return Long.parseLong(value);
    } catch (Exception e) {
      return defaultValue;
    }
  }

  public String asString() {
    return value;
  }
}
