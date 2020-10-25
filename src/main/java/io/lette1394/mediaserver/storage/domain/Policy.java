package io.lette1394.mediaserver.storage.domain;

import static io.lette1394.mediaserver.common.Violations.violation;
import static io.lette1394.mediaserver.storage.domain.LifeCycle.*;

import io.lette1394.mediaserver.common.Testable;
import io.lette1394.mediaserver.common.Tries;
import io.vavr.control.Try;
import java.util.Set;

public interface Policy extends Testable<Snapshot> {

  Policy REJECT_RESUME_UPLOAD = current -> {
    if (current.getLifeCycle().is(BEFORE_UPLOAD) && current.getState().isPending()) {
      return Try.failure(violation("reject resume upload"));
    }
    return Tries.SUCCEED;
  };

  Policy REJECT_OVERWRITE_UPLOAD = current -> {
    if (current.getLifeCycle().is(BEFORE_UPLOAD) && current.getState().isFulfilled()) {
      return Try.failure(violation("reject resume upload"));
    }
    return Tries.SUCCEED;
  };

  Policy REJECT_10MB_SIZE_OVER = current -> {
    if (current.getLifeCycle().is(DURING_UPLOADING) && current.getProgressingSize() > 1024) {
      return Try.failure(
        violation(String.format("Allow under 1K, got: [%s] bytes", current.getProgressingSize())));
    }
    return Tries.SUCCEED;
  };

  Policy REJECT_PENDING_OBJECT = current -> {
    if (current.getLifeCycle().is(BEFORE_DOWNLOAD) && current.getState().isPending()) {
      return Try.failure(violation("Reject pending object download"));
    }
    return Tries.SUCCEED;
  };

  Policy ALL_POLICY = snapshot -> AllMatch.allMatch(Set.of(
    REJECT_RESUME_UPLOAD,
    REJECT_OVERWRITE_UPLOAD,
    REJECT_10MB_SIZE_OVER,
    REJECT_PENDING_OBJECT
  ))
    .test(snapshot);

  Try<Void> test(Snapshot current);
}
