package io.lette1394.mediaserver.storage.domain;

public enum Command {
  NO_OPERATION,

  UPLOAD,
  UPLOAD_APPEND, // FIXME (jaeeun) 2020-11-23: 이것도 마음에 안든다...
  DOWNLOAD,
  COPY;

  public boolean is(Command command) {
    return this == command;
  }
}
