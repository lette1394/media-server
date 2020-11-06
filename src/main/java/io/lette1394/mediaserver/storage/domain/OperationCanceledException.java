package io.lette1394.mediaserver.storage.domain;

import static java.lang.String.format;

import lombok.Getter;

@Getter
public class OperationCanceledException extends RuntimeException {

  private final Command command;

  public OperationCanceledException(Command command, String message, Throwable cause) {
    super(format("command:[%s], %s", command, message), cause);
    this.command = command;
  }

  public OperationCanceledException(Command command, Throwable cause) {
    super(format("command:[%s]", command), cause);
    this.command = command;
  }
}
