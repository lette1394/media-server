package io.lette1394.mediaserver.domain.storage.object;

import io.lette1394.mediaserver.common.Result;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;

public class Policies {
  public static Function<Result, CompletableFuture<Result>> runNextIfPassed(CompletableFuture<Result> future) {
    return result -> {
      if (result.isSucceed()) {
        return future;
      }
      return CompletableFuture.completedFuture(result);
    };
  }

  public static BinaryOperator<CompletableFuture<Result>> mergeAllMatch() {
    return (test1, test2) -> test1.thenCombine(test2, (result1, result2) -> {
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

      return Result.fail("unreachable", new ObjectPolicyViolationException());
    });
  }
}
