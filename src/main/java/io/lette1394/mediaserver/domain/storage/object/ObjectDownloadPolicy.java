package io.lette1394.mediaserver.domain.storage.object;

import static io.lette1394.mediaserver.domain.storage.object.Policies.mergeAllMatch;
import static java.util.concurrent.CompletableFuture.completedFuture;

import io.lette1394.mediaserver.common.Result;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import lombok.Value;

public interface ObjectDownloadPolicy {

  ObjectDownloadPolicy REJECT_PENDING_OBJECT = object -> {
    if (object.isPending()) {
      return completedFuture(
        Result.fail(new ObjectPolicyViolationException("Reject pending object download")));
    }
    return completedFuture(Result.succeed());
  };

  CompletableFuture<Result> test(Object object);

  @Value
  class AllMatch implements ObjectDownloadPolicy {
    Set<ObjectDownloadPolicy> policies;

    public static AllMatch allMatch(ObjectDownloadPolicy... policies) {
      return new AllMatch(Set.of(policies));
    }

    @Override
    public CompletableFuture<Result> test(Object object) {
      return policies
        .stream()
        .map(policy -> policy.test(object))
        .reduce(
          completedFuture(Result.succeed()),
          mergeAllMatch());
    }
  }
}
