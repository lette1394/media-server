package io.lette1394.mediaserver.domain.storage.object;

import io.lette1394.mediaserver.common.Result;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import lombok.Value;

@FunctionalInterface
public interface ObjectUploadPolicy {

  ObjectUploadPolicy ALLOW_RESUME_UPLOAD = (object, storage) -> storage
    .isExist(object.identifier)
    .thenApply(isExist -> isExist ? Result.succeed()
      : Result.fail("", new ObjectPolicyViolationException()));


  ObjectUploadPolicy ALLOW_UNDER_10MB_SIZE = (object, storage) -> CompletableFuture
    .completedFuture(Result.succeed());

  ObjectUploadPolicy ALL = (object, storage) -> new AllMatch(
    Set.of(
      ALLOW_RESUME_UPLOAD,
      ALLOW_UNDER_10MB_SIZE
    )).test(object, storage);

  CompletableFuture<Result> test(Object object, Storage storage);

  @Value
  class AllMatch implements ObjectUploadPolicy {
    Set<ObjectUploadPolicy> policies;

    @Override
    public CompletableFuture<Result> test(Object object, Storage storage) {
      return policies
        .stream()
        .map(policy -> policy.test(object, storage))
        .reduce(
          CompletableFuture.completedFuture(Result.succeed()),
          Policies.mergeAllMatch());
    }
  }
}
