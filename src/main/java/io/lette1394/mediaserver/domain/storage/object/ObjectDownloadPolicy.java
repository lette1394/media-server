package io.lette1394.mediaserver.domain.storage.object;

import static java.util.concurrent.CompletableFuture.completedFuture;

import io.lette1394.mediaserver.common.Result;

public interface ObjectDownloadPolicy extends Testable<Object> {

  ObjectDownloadPolicy REJECT_PENDING_OBJECT = object -> {
    if (object.isPending()) {
      return completedFuture(
        Result.fail(new ObjectPolicyViolationException("Reject pending object download")));
    }
    return completedFuture(Result.succeed());
  };
}
