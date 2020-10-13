package io.lette1394.mediaserver.common;

import lombok.Value;

@Value
public class Result {
  boolean isSucceed;
  String reason;

  public static Result succeed() {
    return new Result(true, "");
  }

  public static Result fail(String reason) {
    return new Result(false, reason);
  }

  public boolean isSucceed() {
    return isSucceed;
  }

  public boolean isFailed() {
    return isSucceed == false;
  }

  public String getReason() {
    if (isSucceed) {
      return "";
    }
    return reason;
  }
}
