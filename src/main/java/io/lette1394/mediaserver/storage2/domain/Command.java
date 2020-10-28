package io.lette1394.mediaserver.storage2.domain;

public enum Command {
  NO_OP,

  UPLOAD,
  DOWNLOAD,
  COPY;

  public boolean is(Command command) {
    return this == command;
  }
}
