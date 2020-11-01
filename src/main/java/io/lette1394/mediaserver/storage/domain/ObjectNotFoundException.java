package io.lette1394.mediaserver.storage.domain;

public class ObjectNotFoundException extends RuntimeException {

  public ObjectNotFoundException() {
    super();
  }

  public ObjectNotFoundException(String message) {
    super(message);
  }

  public ObjectNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public ObjectNotFoundException(Throwable cause) {
    super(cause);
  }
}
