package io.lette1394.mediaserver.storage.domain;

import static io.lette1394.mediaserver.common.Violations.violation;

import io.lette1394.mediaserver.common.Testable;
import io.lette1394.mediaserver.common.Tries;
import io.vavr.control.Try;
import java.util.Set;

public interface ObjectPolicy extends Testable<ObjectSnapshot> {

  ObjectPolicy REJECT_RESUME_UPLOAD = current -> {
    if (current.lifeCycle.isBeforeUpload() && current.state.isPending()) {
      return Try.failure(violation("reject resume upload"));
    }
    return Tries.SUCCEED;
  };

  ObjectPolicy REJECT_OVERWRITE_UPLOAD = current -> {
    if (current.lifeCycle.isBeforeUpload() && current.state.isFulfilled()) {
      return Try.failure(violation("reject resume upload"));
    }
    return Tries.SUCCEED;
  };

  ObjectPolicy REJECT_10MB_SIZE_OVER = current -> {
    if (current.lifeCycle.isDuringUploading() && current.progressingSize > 1024) {
      return Try.failure(
        violation(String.format("Allow under 1K, got: [%s] bytes", current.progressingSize)));
    }
    return Tries.SUCCEED;
  };

  ObjectPolicy REJECT_PENDING_OBJECT = current -> {
    if (current.lifeCycle.isBeforeDownload() && current.state.isPending()) {
      return Try.failure(violation("Reject pending object download"));
    }
    return Tries.SUCCEED;
  };

  ObjectPolicy ALL_POLICY = snapshot -> AllMatch.allMatch(Set.of(
    REJECT_RESUME_UPLOAD,
    REJECT_OVERWRITE_UPLOAD,
    REJECT_10MB_SIZE_OVER,
    REJECT_PENDING_OBJECT
  ))
    .test(snapshot);

  Try<Void> test(ObjectSnapshot current);
}
