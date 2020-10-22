package io.lette1394.mediaserver.domain.storage.object;

import static java.util.concurrent.CompletableFuture.completedFuture;

import io.lette1394.mediaserver.common.Result;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import lombok.Value;

@FunctionalInterface
public interface BiTestable<T, U> {
  CompletableFuture<Result<Void>> test(T t, U u);

  @Value
  class AllMatch<T, U> implements BiTestable<T, U> {
    Set<BiTestable<T, U>> policies;

    public static <T, U> AllMatch<T, U> allMatch(Set<BiTestable<T, U>> policies) {
      return new AllMatch<T, U>(policies);
    }

    @Override
    public CompletableFuture<Result<Void>> test(T t, U u) {
      return policies
        .stream()
        .map(policy -> policy.test(t, u))
        .reduce(
          completedFuture(Result.succeed()),
          Policies.mergeAllMatch());
    }
  }
}
