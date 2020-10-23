package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.Result;
import java.util.Set;
import lombok.Value;

@FunctionalInterface
public interface Testable<T> {
  Result<Void> test(T t);

  @Value
  class AllMatch<T> implements Testable<T> {
    Set<Testable<T>> policies;

    public static <T> AllMatch<T> allMatch(Set<Testable<T>> policies) {
      return new AllMatch<T>(policies);
    }

    @Override
    public Result<Void> test(T t) {
      return policies
        .stream()
        .map(policy -> policy.test(t))
        .reduce(
          Result.succeed(),
          Policies.mergeAllMatch());
    }
  }
}
