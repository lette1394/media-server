package io.lette1394.mediaserver.common;

import io.vavr.control.Try;
import java.util.Set;
import lombok.Value;

@FunctionalInterface
public interface BiTestable<T, U> {
  Try<Void> test(T t, U u);

  @Value
  class AllMatch<T, U> implements BiTestable<T, U> {
    Set<BiTestable<T, U>> policies;

    public static <T, U> AllMatch<T, U> allMatch(Set<BiTestable<T, U>> policies) {
      return new AllMatch<T, U>(policies);
    }

    @Override
    public Try<Void> test(T t, U u) {
      return policies
        .stream()
        .map(policy -> policy.test(t, u))
        .reduce(
          Try.success(null),
          Tries.mergeAllMatch());
    }
  }
}
