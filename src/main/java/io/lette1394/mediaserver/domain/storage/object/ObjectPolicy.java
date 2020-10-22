package io.lette1394.mediaserver.domain.storage.object;

import static io.lette1394.mediaserver.common.Result.fail;
import static io.lette1394.mediaserver.common.Result.succeed;
import static io.lette1394.mediaserver.domain.storage.object.Violations.violation;
import static java.util.concurrent.CompletableFuture.completedFuture;

import io.lette1394.mediaserver.common.Result;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface ObjectPolicy extends Testable<ObjectSnapshot> {
  ObjectPolicy REJECT_RESUME_UPLOAD = current -> {
    if (current.lifeCycle.isBeforeUploading() && current.state.isPending()) {
      return completedFuture(fail(violation("reject resume upload")));
    }
    return completedFuture(succeed());
  };
  ObjectPolicy ALLOW_UNDER_10MB_SIZE = current -> {
    if (current.lifeCycle.isAfterUploaded() && current.size < 1024 * 1024 * 10) {
      return completedFuture(succeed());
    }
    return completedFuture(fail(violation("Allow under 10MB")));
  };
  ObjectPolicy REJECT_PENDING_OBJECT = current -> {
    if (current.lifeCycle.isBeforeDownloading() && current.state.isPending()) {
      return completedFuture(fail(violation("Reject pending object download")));
    }
    return completedFuture(succeed());
  };
  ObjectPolicy ALL_POLICY = snapshot -> AllMatch.allMatch(Set.of(

    REJECT_RESUME_UPLOAD,
    ALLOW_UNDER_10MB_SIZE,
    REJECT_PENDING_OBJECT

  ))
    .test(snapshot);

  CompletableFuture<Result> test(ObjectSnapshot current);
}
