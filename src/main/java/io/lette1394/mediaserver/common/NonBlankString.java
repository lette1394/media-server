package io.lette1394.mediaserver.common;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import lombok.Value;

@Value
public class NonBlankString {
  String value;

  public NonBlankString(String value) {
    Contracts.require(isNotBlank(value), format("NonEmtpyString cannot be blank, got %s", value));
    this.value = value;
  }

  public static NonBlankString nonBlankString(String value) {
    return new NonBlankString(value);
  }
}
