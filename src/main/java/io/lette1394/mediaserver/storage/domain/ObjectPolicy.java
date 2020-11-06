package io.lette1394.mediaserver.storage.domain;

import static io.lette1394.mediaserver.common.Tries.SUCCESS;
import static io.lette1394.mediaserver.common.Violations.Code.INVALID_OBJECT_STATE;
import static io.lette1394.mediaserver.common.Violations.violation;
import static io.lette1394.mediaserver.storage.domain.Command.DOWNLOAD;
import static io.lette1394.mediaserver.storage.domain.Command.UPLOAD;
import static io.lette1394.mediaserver.storage.domain.ObjectType.FULFILLED;
import static io.lette1394.mediaserver.storage.domain.ObjectType.PENDING;
import static io.vavr.control.Try.failure;

import io.lette1394.mediaserver.common.Testable;
import io.vavr.control.Try;
import java.util.Set;

public interface ObjectPolicy extends Testable<ObjectSnapshot> {

  ObjectPolicy REJECT_RESUME_UPLOAD = snapshot -> {
    if (snapshot.is(UPLOAD) && snapshot.is(PENDING)) {
      return failure(violation(INVALID_OBJECT_STATE, "reject resume upload"));
    }
    return SUCCESS;
  };

  ObjectPolicy REJECT_OVERWRITE_UPLOAD = snapshot -> {
    if (snapshot.is(UPLOAD) && snapshot.is(FULFILLED)) {
      return failure(violation(INVALID_OBJECT_STATE, "reject overwrite upload"));
    }
    return SUCCESS;
  };

  ObjectPolicy REJECT_PARTIAL_DOWNLOAD = snapshot -> {
    if (snapshot.is(DOWNLOAD) && snapshot.is(PENDING)) {
      return failure(violation(INVALID_OBJECT_STATE, "Reject pending object download"));
    }
    return SUCCESS;
  };

  ObjectPolicy ALL_OBJECT_POLICY = objectSnapshot -> AllMatch.allMatch(Set.of(
    REJECT_RESUME_UPLOAD,
    REJECT_OVERWRITE_UPLOAD,
    REJECT_PARTIAL_DOWNLOAD
  ))
    .test(objectSnapshot);

  Try<Void> test(ObjectSnapshot snapshot);
}
