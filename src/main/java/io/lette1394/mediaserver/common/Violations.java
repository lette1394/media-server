package io.lette1394.mediaserver.common;

public class Violations {

  public static PolicyViolationException violation(String reason) {
    return new PolicyViolationException(reason);
  }
}
