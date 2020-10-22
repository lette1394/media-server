package io.lette1394.mediaserver.domain.storage.object;

public class Violations {

  public static ObjectPolicyViolationException violation(String reason) {
    return new ObjectPolicyViolationException(reason);
  }
}
