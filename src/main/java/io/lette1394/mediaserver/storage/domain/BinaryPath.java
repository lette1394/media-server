package io.lette1394.mediaserver.storage.domain;

@FunctionalInterface
public interface BinaryPath {

  static BinaryPath from(Identifier identifier) {
    return () -> String.format("%s/%s", identifier.getArea(), identifier.getKey());
  }

  static BinaryPath from(String area, String key) {
    return from(new Identifier(area, key));
  }

  static BinaryPath from(String key) {
    return () -> key;
  }

  String asString();
}
