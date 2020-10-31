package io.lette1394.mediaserver.storage.domain;

import static io.lette1394.mediaserver.matchers.Matchers.causeIs;
import static io.lette1394.mediaserver.matchers.Matchers.commandIs;
import static io.lette1394.mediaserver.matchers.ObjectMatchers.got;
import static io.lette1394.mediaserver.storage.domain.Command.UPLOAD;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.lette1394.mediaserver.storage.domain.Events.UploadRejected;
import io.vavr.control.Try;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Object class")
class ObjectTest {
  private Object<?> anyObject(ObjectPolicy objectPolicy) {
    final ObjectFactory<?> objectFactory = new ObjectFactory<>(objectPolicy, null);
    return objectFactory.create(anyIdentifier());
  }

  private Identifier anyIdentifier() {
    return new Identifier(randomAlphanumeric(10), randomAlphanumeric(10));
  }

  @Nested
  @DisplayName("upload()는")
  class Upload {
    @Nested
    @DisplayName("정책을 위반한 경우")
    class Context_with_illegal_policy {
      @Test
      @DisplayName("예외를 던지고 upload rejected 이벤트가 발생한다")
      void it_throws_exception() {
        final ObjectPolicy rejectAll = current -> Try.failure(new RuntimeException("reject"));
        final Object<?> object = anyObject(rejectAll);

        final OperationCanceled exception = assertThrows(OperationCanceled.class,
          () -> object.upload(null));

        assertThat(exception, commandIs(UPLOAD));
        assertThat(exception, causeIs(RuntimeException.class));
        assertThat(object, got(UploadRejected.class));
      }
    }
  }
}