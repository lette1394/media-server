package io.lette1394.mediaserver.storage.domain;

public class PolicyViolationException extends RuntimeException {
  public PolicyViolationException() {
    super();
  }

  public PolicyViolationException(String message) {
    super(message);
  }

  public PolicyViolationException(String message, Throwable cause) {
    super(message, cause);
  }

  public PolicyViolationException(Throwable cause) {
    super(cause);
  }
}
