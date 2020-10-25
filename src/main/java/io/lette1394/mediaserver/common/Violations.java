package io.lette1394.mediaserver.common;

import io.lette1394.mediaserver.common.PolicyViolationException;

public class Violations {

  public static PolicyViolationException violation(String reason) {
    return new PolicyViolationException(reason);
  }
}
