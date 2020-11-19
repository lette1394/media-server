package io.lette1394.mediaserver.utils;

import io.lette1394.mediaserver.common.TimeStamp;
import io.lette1394.mediaserver.storage.domain.BinaryPath;
import io.lette1394.mediaserver.storage.domain.BinaryPolicy;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.ObjectPolicy;
import io.vavr.control.Try;
import java.time.OffsetDateTime;
import org.apache.commons.lang3.RandomStringUtils;

public class TestUtils {
  public static Identifier anyIdentifier() {
    return new Identifier(RandomStringUtils.randomAlphanumeric(10), RandomStringUtils.randomAlphanumeric(10));
  }

  public static Identifier randomIdentifier() {
    return anyIdentifier();
  }

  public static BinaryPath anyBinaryPath() {
    return new BinaryPath(RandomStringUtils.randomAlphanumeric(10));
  }

  public static TimeStamp anyTimeStamp() {
    return new TimeStamp(OffsetDateTime.now(), OffsetDateTime.now());
  }

  public static ObjectPolicy ALLOW_OBJECT_POLICY = snapshot -> Try.success(null);
  public static BinaryPolicy ALLOW_BINARY_POLICY = binarySnapshot -> Try.success(null);
}
