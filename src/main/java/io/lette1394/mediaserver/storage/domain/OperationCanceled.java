package io.lette1394.mediaserver.storage.domain;

import static java.lang.String.format;

import lombok.Getter;

@Getter
public class OperationCanceled extends RuntimeException {

  private final Command command;

  public OperationCanceled(Command command) {
    super(format("%s", command));
    this.command = command;
  }

  public OperationCanceled(Command command, String message) {
    super(format("command:[%s], %s", command, message));
    this.command = command;
  }

  public OperationCanceled(Command command, String message, Throwable cause) {
    super(format("command:[%s], %s", command, message), cause);
    this.command = command;
  }

  public OperationCanceled(Command command, Throwable cause) {
    super(format("command:[%s]", command), cause);
    this.command = command;
  }
}
