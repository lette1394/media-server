package io.lette1394.mediaserver.common;

import static java.lang.String.format;

import lombok.Getter;

@Getter
public class PolicyViolationException extends RuntimeException {
  private final String code;
  private final String message;

  public PolicyViolationException(String code, String message) {
    super(format("[%s] %s", code, message));
    this.code = code;
    this.message = message;
  }
}
