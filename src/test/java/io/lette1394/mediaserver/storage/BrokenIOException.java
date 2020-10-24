package io.lette1394.mediaserver.storage;

import java.io.IOException;

public class BrokenIOException extends IOException {
  public BrokenIOException(String message) {
    super(message);
  }
}
