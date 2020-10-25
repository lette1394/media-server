package io.lette1394.mediaserver.common;

public class UnknownException extends RuntimeException {
  public static final UnknownException INSTANCE = new UnknownException();

  public UnknownException() {
    super();
  }

  public UnknownException(String message) {
    super(message);
  }

  public UnknownException(String message, Throwable cause) {
    super(message, cause);
  }
}
