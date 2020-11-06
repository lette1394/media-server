package io.lette1394.mediaserver.common;

import io.vavr.control.Try;
import java.util.function.BinaryOperator;

public class Tries {
  public static final Try<Void> SUCCESS = Try.success(null);

  public static BinaryOperator<Try<Void>> mergeAllMatch() {
    return Tries::mergeAllMatch;
  }

  private static Try<Void> mergeAllMatch(Try<Void> result1, Try<Void> result2) {
    if (result1.isSuccess() && result2.isSuccess()) {
      return Tries.SUCCESS;
    }

    // TODO: multi reason container
    if (result1.isFailure()) {
      return result1;
    }
    if (result2.isFailure()) {
      return result2;
    }

    return Try.failure(new PolicyViolationException("", ""));
  }
}
