package io.lette1394.mediaserver.storage.domain;

import static io.lette1394.mediaserver.matchers.Matchers.causeIs;
import static io.lette1394.mediaserver.matchers.Matchers.commandIs;
import static io.lette1394.mediaserver.matchers.ObjectMatchers.got;
import static io.lette1394.mediaserver.matchers.ObjectMatchers.is;
import static io.lette1394.mediaserver.storage.domain.Command.UPLOAD;
import static io.lette1394.mediaserver.storage.domain.ObjectType.FULFILLED;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.lette1394.mediaserver.storage.domain.Events.UploadRejected;
import io.lette1394.mediaserver.storage.domain.Events.Uploaded;
import io.lette1394.mediaserver.storage.domain.Events.UploadingTriggered;
import io.lette1394.mediaserver.storage.infrastructure.StringPayload;
import io.vavr.control.Try;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

@DisplayName("Object class")
class ObjectTest {
  private <T extends Payload> Object<T> anyObject() {
    return anyObject(__ -> Try.success(null), __ -> Try.success(null));
  }

  private <T extends Payload> Object<T> anyObject(ObjectPolicy objectPolicy) {
    return anyObject(objectPolicy, __ -> Try.success(null));
  }

  private <T extends Payload> Object<T> anyObject(ObjectPolicy objectPolicy,
    BinaryPolicy binaryPolicy) {
    final ObjectFactory<T> objectFactory = new ObjectFactory<>(objectPolicy, binaryPolicy);
    return objectFactory.create(anyIdentifier());
  }

  private Identifier anyIdentifier() {
    return new Identifier(randomAlphanumeric(10), randomAlphanumeric(10));
  }

  private Publisher<StringPayload> anyStringPublisher() {
    return anyStringPublisher(randomAlphanumeric(10));
  }

  private Publisher<StringPayload> anyStringPublisher(String string) {
    return Flux.fromStream(Arrays.stream(string.split("")).map(StringPayload::new));
  }

  private <T extends Payload> void subscribe(BinarySupplier<T> binarySupplier) {
    Flux.from(binarySupplier.getAsync())
      .subscribe(__ -> {});
  }

  @Nested
  @DisplayName("upload()는")
  class Upload {
    @Nested
    @DisplayName("연산에 성공하면")
    class Context_with_legal_policy {
      @Test
      @DisplayName("성공 이벤트를 만든다")
      void it_produces_successful_events() {
        final Object<StringPayload> object = anyObject();
        final BinarySupplier<StringPayload> binarySupplier = object.upload(anyStringPublisher());

        subscribe(binarySupplier);

        assertThat(object, got(List.of(UploadingTriggered.class, Uploaded.class)));
      }

      @Test
      @DisplayName("FULFILLED 상태를 갖는다")
      void it_() {
        final Object<StringPayload> object = anyObject();
        final BinarySupplier<StringPayload> binarySupplier = object.upload(anyStringPublisher());

        subscribe(binarySupplier);

        assertThat(object, is(FULFILLED));
      }
    }

    @Nested
    @DisplayName("정책을 위반한 경우")
    class Context_with_illegal_policy {
      @Test
      @DisplayName("예외를 던지고 upload rejected 이벤트를 만든다")
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