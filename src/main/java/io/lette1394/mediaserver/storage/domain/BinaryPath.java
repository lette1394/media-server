package io.lette1394.mediaserver.storage.domain;

public class BinaryPath {

  public static BinaryPath from(Identifier identifier) {
    return new BinaryPath() {
      @Override
      public String asString() {
        return String.format("%s/%s", identifier.getArea(), identifier.getKey());
      }
    };
  }

  public String asString() {
    return null;
  }
}
