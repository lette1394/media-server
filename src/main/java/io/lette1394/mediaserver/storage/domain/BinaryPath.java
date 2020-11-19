package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.Contracts;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

@EqualsAndHashCode
public class BinaryPath {
  private final String value;

  public BinaryPath(String value) {
    Contracts.require(StringUtils.isNotBlank(value), "isNotBlank(value)");
    this.value = value;
  }

  public static BinaryPath from(Identifier identifier) {
    return new BinaryPath(String.format("%s/%s", identifier.getArea(), identifier.getKey()));
  }

  public static BinaryPath from(String area, String key) {
    return from(new Identifier(area, key));
  }

  public static BinaryPath from(String key) {
    return new BinaryPath(key);
  }

  public String asString() {
    return value;
  }
}
