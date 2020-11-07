package io.lette1394.mediaserver.storage.domain;

public abstract class BinaryPath {

  public static BinaryPath from(Identifier identifier) {
    return new BinaryPath() {
      @Override
      public String asString() {
        return String.format("%s/%s", identifier.getArea(), identifier.getKey());
      }
    };
  }

  public static BinaryPath from(String area, String key) {
    return from(new Identifier(area, key));
  }

  public abstract String asString();
}
