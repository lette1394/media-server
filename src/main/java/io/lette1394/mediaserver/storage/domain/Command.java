package io.lette1394.mediaserver.storage.domain;

public enum Command {
  NO_OPERATION,

  UPLOAD,
  DOWNLOAD,
  COPY;

  public boolean is(Command command) {
    return this == command;
  }
}
