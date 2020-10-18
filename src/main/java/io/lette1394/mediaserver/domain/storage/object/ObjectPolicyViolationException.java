package io.lette1394.mediaserver.domain.storage.object;

public class ObjectPolicyViolationException extends RuntimeException {
  public ObjectPolicyViolationException() {
    super();
  }

  public ObjectPolicyViolationException(String message) {
    super(message);
  }

  public ObjectPolicyViolationException(String message, Throwable cause) {
    super(message, cause);
  }

  public ObjectPolicyViolationException(Throwable cause) {
    super(cause);
  }
}
