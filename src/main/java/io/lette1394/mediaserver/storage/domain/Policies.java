package io.lette1394.mediaserver.storage.domain;

import static java.util.concurrent.CompletableFuture.completedFuture;

import io.lette1394.mediaserver.common.Result;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

public class Policies {
  public static <T, R> Function<Result<T>, CompletableFuture<Result<R>>>
  runNextIfPassedAsync(Supplier<CompletableFuture<Result<R>>> next) {
    return previous -> {
      if (previous.isSucceed()) {
        return next.get();
      }
      return completedFuture(Result.fail(previous.getReason(), previous.getThrowable()));
    };
  }

  public static <T, R> Function<Result<T>, Result<R>>
  runNextIfPassed(Supplier<Result<R>> next) {
    return previous -> {
      if (previous.isSucceed()) {
        return next.get();
      }
      return Result.fail(previous.getReason(), previous.getThrowable());
    };
  }

  public static <T, R> CompletableFuture<Result<Void>>
  runIfPassed(Result<T> result) {
    if (result.isFailed()) {
      return CompletableFuture.failedFuture(result.getThrowable());
    }
    return CompletableFuture.completedFuture(null);
  }

  public static BinaryOperator<CompletableFuture<Result<Void>>> mergeAllMatchAsync() {
    return (test1, test2) -> test1.thenCombine(test2, Policies::mergeAllMatch);
  }

  public static BinaryOperator<Result<Void>> mergeAllMatch() {
    return Policies::mergeAllMatch;
  }

  private static Result<Void> mergeAllMatch(Result<Void> result1, Result<Void> result2) {
    if (result1.isSucceed() && result2.isSucceed()) {
      return Result.succeed();
    }

    // TODO: multi reason container
    if (result1.isFailed()) {
      return result1;
    }
    if (result2.isFailed()) {
      return result2;
    }

    return Result.fail("unreachable", new PolicyViolationException());
  }
}
