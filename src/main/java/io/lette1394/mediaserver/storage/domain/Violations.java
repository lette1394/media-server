package io.lette1394.mediaserver.storage.domain;

public class Violations {

  public static PolicyViolationException violation(String reason) {
    return new PolicyViolationException(reason);
  }
}
