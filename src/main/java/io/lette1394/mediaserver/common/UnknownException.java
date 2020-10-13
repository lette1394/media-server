package io.lette1394.mediaserver.common;

public class UnknownException extends RuntimeException {
  public static final UnknownException INSTANCE = new UnknownException();

  private UnknownException() {}
}
