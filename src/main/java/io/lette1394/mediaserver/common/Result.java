package io.lette1394.mediaserver.common;

import lombok.Value;

@Value
public class Result {
  private static final Result SUCCEED = new Result(true, "", null);

  boolean isSucceed;

  String reason;
  Throwable throwable;

  public static Result succeed() {
    return SUCCEED;
  }

  public static Result fail(Throwable throwable) {
    return new Result(false, "", throwable);
  }

  public static Result fail(String reason, Throwable throwable) {
    return new Result(false, reason, throwable);
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
