package io.lette1394.mediaserver.common;

public class Violations {

  public static PolicyViolationException violation(String code, String reason) {
    return new PolicyViolationException(code, reason);
  }

  public static class Code {
    public static final String INVALID_OBJECT_STATE = "InvalidObjectState";
    public static final String MISMATCHED_CONTENT_LENGTH = "MismatchedContentLength";
    public static final String ENTITY_TOO_LARGE = "EntityTooLarge";
    public static final String INVALID_IDENTIFIER = "InvalidIdentifier";
  }
}
