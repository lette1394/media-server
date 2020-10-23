package io.lette1394.mediaserver.storage.domain;

public class Violations {

  public static ObjectPolicyViolationException violation(String reason) {
    return new ObjectPolicyViolationException(reason);
  }
}
