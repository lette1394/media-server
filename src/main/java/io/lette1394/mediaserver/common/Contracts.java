package io.lette1394.mediaserver.common;

public class Contracts {

  public static void require(boolean condition, String message) {
    if (condition) {
      return;
    }
    throw new ContractViolationException(String.format("required: %s", message));
  }
}
