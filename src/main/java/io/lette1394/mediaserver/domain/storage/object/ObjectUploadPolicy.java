package io.lette1394.mediaserver.domain.storage.object;

import static java.util.concurrent.CompletableFuture.completedFuture;

import io.lette1394.mediaserver.common.Result;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import lombok.Value;

@FunctionalInterface
public interface ObjectUploadPolicy {

  ObjectUploadPolicy REJECT_RESUME_UPLOAD = (object, storage) -> storage
    .binaryExists(object)
    .thenApply(isExist -> {
      if (isExist && object.isPending()) {
        return Result.fail(new ObjectPolicyViolationException("reject resume upload"));
      }
      return Result.succeed();
    });


  ObjectUploadPolicy ALLOW_UNDER_10MB_SIZE = (object, storage) -> {
    // TODO: 음... 크기를 미리 알 수 있나?
    if (object.getSize() > 1024 * 1024 * 10) {
      return completedFuture(Result.fail(new ObjectPolicyViolationException("Allow under 10MB")));
    }
    return completedFuture(Result.succeed());
  };

  ObjectUploadPolicy ALL = (object, storage) -> new AllMatch(
    Set.of(
      REJECT_RESUME_UPLOAD,
      ALLOW_UNDER_10MB_SIZE
    )).test(object, storage);

  CompletableFuture<Result> test(Object object, BinaryRepository binaryRepository);

  @Value
  class AllMatch implements ObjectUploadPolicy {
    Set<ObjectUploadPolicy> policies;

    @Override
    public CompletableFuture<Result> test(Object object, BinaryRepository binaryRepository) {
      return policies
        .stream()
        .map(policy -> policy.test(object, binaryRepository))
        .reduce(
          completedFuture(Result.succeed()),
          Policies.mergeAllMatch());
    }
  }
}
