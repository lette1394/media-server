package io.lette1394.mediaserver.storage.domain;

import static java.lang.String.format;

public class OperationCanceled extends RuntimeException {

  Command command;

  public OperationCanceled(Command command) {
    super(format("%s", command));
  }

  public OperationCanceled(Command command, String message) {
    super(format("command:[%s], %s", command, message));
  }

  public OperationCanceled(Command command, String message, Throwable cause) {
    super(format("command:[%s], %s", command, message), cause);
  }

  public OperationCanceled(Command command, Throwable cause) {
    super(format("command:[%s]", command), cause);
  }
}
