package io.lette1394.mediaserver.storage.domain;

import static io.lette1394.mediaserver.common.Result.fail;
import static io.lette1394.mediaserver.common.Result.succeed;
import static io.lette1394.mediaserver.storage.domain.Violations.violation;

import io.lette1394.mediaserver.common.Result;
import java.util.Set;

public interface ObjectPolicy extends Testable<ObjectSnapshot> {

  ObjectPolicy REJECT_RESUME_UPLOAD = current -> {
    if (current.lifeCycle.isBeforeUpload() && current.state.isPending()) {
      return fail(violation("reject resume upload"));
    }
    return succeed();
  };

  ObjectPolicy REJECT_OVERWRITE_UPLOAD = current -> {
    if (current.lifeCycle.isBeforeUpload() && current.state.isFulfilled()) {
      return fail(violation("reject resume upload"));
    }
    return succeed();
  };

  ObjectPolicy REJECT_10MB_SIZE_OVER = current -> {
    if (current.lifeCycle.isDuringUploading() && current.progressingSize > 1024) {
      return fail(violation(String.format("Allow under 1K, got: [%s] bytes", current.progressingSize)));
    }
    return succeed();
  };

  ObjectPolicy REJECT_PENDING_OBJECT = current -> {
    if (current.lifeCycle.isBeforeDownload() && current.state.isPending()) {
      return fail(violation("Reject pending object download"));
    }
    return succeed();
  };

  ObjectPolicy ALL_POLICY = snapshot -> AllMatch.allMatch(Set.of(
    REJECT_RESUME_UPLOAD,
    REJECT_OVERWRITE_UPLOAD,
    REJECT_10MB_SIZE_OVER,
    REJECT_PENDING_OBJECT
  ))
    .test(snapshot);

  Result<Void> test(ObjectSnapshot current);
}
