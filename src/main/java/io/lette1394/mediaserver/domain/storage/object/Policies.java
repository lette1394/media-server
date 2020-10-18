package io.lette1394.mediaserver.domain.storage.object;

import io.lette1394.mediaserver.common.Result;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;

public class Policies {
  public static Consumer<Result> allowIfPassed() {
    return result -> {
      if (result.isSucceed()) {
        return;
      }
      throw new RuntimeException(result.getThrowable());
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
