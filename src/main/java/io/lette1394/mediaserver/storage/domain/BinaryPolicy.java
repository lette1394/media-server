package io.lette1394.mediaserver.storage.domain;

import static io.lette1394.mediaserver.common.Violations.violation;

import io.lette1394.mediaserver.common.Testable;
import io.lette1394.mediaserver.common.Tries;
import io.vavr.control.Try;
import java.util.Set;

public interface BinaryPolicy extends Testable<BinarySnapshot> {

  BinaryPolicy REJECT_10GB_OVER = binary -> {
    if (binary.isOver(10L * 1024 * 1024 * 1024)) {
      return Try.failure(violation("reject 10GB over binary"));
    }
    return Tries.SUCCESS;
  };

  BinaryPolicy ALL_BINARY_POLICY = binarySnapshot -> AllMatch.allMatch(
    Set.of(
      REJECT_10GB_OVER
    ))
    .test(binarySnapshot);
}
