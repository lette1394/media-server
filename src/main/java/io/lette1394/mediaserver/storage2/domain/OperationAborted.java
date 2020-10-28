package io.lette1394.mediaserver.storage2.domain;

import static java.lang.String.format;

public class OperationAborted extends RuntimeException {

  public OperationAborted(Command command, String message, Throwable cause) {
    super(format("%s, command:[%s]", message, command), cause);
  }

  public OperationAborted(Command command, Throwable cause) {
    super(format("command:[%s]", command), cause);
  }
}
