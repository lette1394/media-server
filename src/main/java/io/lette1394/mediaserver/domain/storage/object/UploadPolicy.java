package io.lette1394.mediaserver.domain.storage.object;

import static java.util.concurrent.CompletableFuture.completedFuture;

import io.lette1394.mediaserver.common.Result;

public interface UploadPolicy {

  ObjectUploadPolicy REJECT_RESUME_UPLOAD = (object, binaryRepository) -> binaryRepository
    .binaryExists(object)
    .thenApply(isExist -> {
      if (isExist && object.isPending()) {
        return Result.fail(new ObjectPolicyViolationException("reject resume upload"));
      }
      return Result.succeed();
    });
  ObjectUploadPolicy ALLOW_UNDER_10MB_SIZE = (object, storage) -> {
    if (object.getSize() < 1024 * 1024 * 10) {
      return completedFuture(Result.succeed());
    }
    return completedFuture(Result.fail(new ObjectPolicyViolationException("Allow under 10MB")));
  };

  @FunctionalInterface
  interface ObjectUploadPolicy extends BiTestable<Object, BinaryRepository> {
  }
}
