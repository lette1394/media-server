package io.lette1394.mediaserver.storage.domain;

import static io.lette1394.mediaserver.common.Tries.SUCCESS;
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

  ObjectPolicy REJECT_RESUME_UPLOAD = object -> {
    if (object.is(UPLOAD) && object.is(PENDING)) {
      return failure(violation("reject resume upload"));
    }
    return SUCCESS;
  };

  ObjectPolicy REJECT_OVERWRITE_UPLOAD = object -> {
    if (object.is(UPLOAD) && object.is(FULFILLED)) {
      return failure(violation("reject overwrite upload"));
    }
    return SUCCESS;
  };

  // TODO: binary policy로 빼기
//  Policy REJECT_10MB_SIZE_OVER = current -> {
//    if (current.is(DURING_UPLOADING) && current.getProgressingSize() > 1024 * 1024 * 10) {
//      return failure(
//        violation(format("Allow under 1K, got: [%s] bytes", current.getProgressingSize())));
//    }
//    return SUCCESS;
//  };

  ObjectPolicy REJECT_PARTIAL_DOWNLOAD = object -> {
    if (object.is(DOWNLOAD) && object.is(PENDING)) {
      return failure(violation("Reject pending object download"));
    }
    return SUCCESS;
  };

  ObjectPolicy ALL_OBJECT_POLICY = objectSnapshot -> AllMatch.allMatch(Set.of(
    REJECT_RESUME_UPLOAD,
    REJECT_OVERWRITE_UPLOAD,
    REJECT_PARTIAL_DOWNLOAD
  ))
    .test(objectSnapshot);

  Try<Void> test(ObjectSnapshot current);
}
