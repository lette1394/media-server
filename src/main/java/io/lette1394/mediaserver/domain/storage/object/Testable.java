package io.lette1394.mediaserver.domain.storage.object;

import static io.lette1394.mediaserver.domain.storage.object.Policies.mergeAllMatch;
import static java.util.concurrent.CompletableFuture.completedFuture;

import io.lette1394.mediaserver.common.Result;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import lombok.Value;

@FunctionalInterface
public interface Testable<T> {
  CompletableFuture<Result<Void>> test(T t);

  @Value
  class AllMatch<T> implements Testable<T> {
    Set<Testable<T>> policies;

    public static <T> AllMatch<T> allMatch(Set<Testable<T>> policies) {
      return new AllMatch<T>(policies);
    }

    @Override
    public CompletableFuture<Result<Void>> test(T t) {
      return policies
        .stream()
        .map(policy -> policy.test(t))
        .reduce(
          completedFuture(Result.succeed()),
          mergeAllMatch());
    }
  }
}
