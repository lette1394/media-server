package io.lette1394.mediaserver.common;

import java.util.NoSuchElementException;
import lombok.Value;

@Value
public class Result<T> {
  private static final Result<Void> SUCCEED = new Result<>(true, null, "", null);

  boolean isSucceed;
  T value;

  String reason;
  Throwable throwable;

  public static Result<Void> succeed() {
    return SUCCEED;
  }

  public static <T> Result<T> succeed(T value) {
    return new Result<T>(true, value, "", null);
  }

  public static <T> Result<T> fail(Throwable throwable) {
    return new Result<>(false, null, "", throwable);
  }

  public static <T> Result<T> fail(String reason, Throwable throwable) {
    return new Result<>(false, null, reason, throwable);
  }

  public boolean isSucceed() {
    return isSucceed;
  }

  public boolean isFailed() {
    return isSucceed == false;
  }

  public T get() {
    if (isSucceed) {
      if (value == null) {
        throw new NoSuchElementException("No value present");
      }
      return value;
    }
    throw new IllegalStateException("Not succeeded");
  }

  public String getReason() {
    if (isSucceed) {
      return "";
    }
    return reason;
  }
}
