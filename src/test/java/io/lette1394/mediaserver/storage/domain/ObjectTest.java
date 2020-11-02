package io.lette1394.mediaserver.storage.domain;

import static io.lette1394.mediaserver.matchers.Matchers.causeIs;
import static io.lette1394.mediaserver.matchers.Matchers.commandIs;
import static io.lette1394.mediaserver.matchers.ObjectMatchers.got;
import static io.lette1394.mediaserver.matchers.ObjectMatchers.hasSize;
import static io.lette1394.mediaserver.matchers.ObjectMatchers.hasType;
import static io.lette1394.mediaserver.storage.domain.Command.UPLOAD;
import static io.lette1394.mediaserver.storage.domain.ObjectType.FULFILLED;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.lette1394.mediaserver.storage.domain.Events.UploadRejected;
import io.lette1394.mediaserver.storage.domain.Events.Uploaded;
import io.lette1394.mediaserver.storage.domain.Events.UploadingTriggered;
import io.lette1394.mediaserver.storage.infrastructure.StringPayload;
import io.vavr.control.Try;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
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

  private <T extends Payload> Object<T> anyObject(BinaryPolicy binaryPolicy) {
    return anyObject(__ -> Try.success(null), binaryPolicy);
  }

  private <T extends Payload> Object<T> anyObject(ObjectPolicy objectPolicy,
    BinaryPolicy binaryPolicy) {
    final ObjectFactory<T> objectFactory = new ObjectFactory<>(objectPolicy, binaryPolicy);
    return objectFactory.create(anyIdentifier());
  }

  private Identifier anyIdentifier() {
    return new Identifier(randomAlphanumeric(10), randomAlphanumeric(10));
  }

  private Publisher<StringPayload> anyStringPublisher(String string) {
    return Flux.fromStream(Arrays.stream(string.split("")).map(StringPayload::new));
  }

  private <T extends Payload> void subscribe(BinarySupplier<T> binarySupplier) {
    Flux.from(binarySupplier.getAsync())
      .subscribe(__ -> {
      });
  }

  @Nested
  @DisplayName("upload()")
  class Upload {

    @Nested
    @DisplayName("연산 중에")
    class Context_during_operation {
      @Test
      @DisplayName("Command")
      void it_produces_successful_events() {
        final AtomicReference<ObjectSnapshot> ret = new AtomicReference<>();

        final ObjectPolicy objectPolicy = snapshot -> {
          ret.set(snapshot);
          return Try.success(null);
        };
        final Object<StringPayload> object = anyObject(objectPolicy);
        object.upload(null);

        assertThat(ret.get().getCommand(), is(UPLOAD));
      }
    }

    @Nested
    @DisplayName("연산에 성공하면")
    class Context_with_legal_policy {
      @Test
      @DisplayName("성공 이벤트를 만든다")
      void it_produces_successful_events() {
        final Object<StringPayload> object = subject();
        assertThat(object, got(List.of(UploadingTriggered.class, Uploaded.class)));
      }

      @Test
      @DisplayName("FULFILLED 타입을 갖는다")
      void it_has_fulfilled_type() {
        final Object<StringPayload> object = subject();
        assertThat(object, hasType(FULFILLED));
      }

      @Test
      @DisplayName("payload 길이를 갖는다")
      void it_proper_length() {
        final int length = 10;
        final Object<StringPayload> object = subject(randomAlphanumeric(length));
        assertThat(object, hasSize(length));
      }

      private Object<StringPayload> subject() {
        return subject(randomAlphanumeric(10));
      }

      private Object<StringPayload> subject(String payload) {
        final Object<StringPayload> object = anyObject();
        final BinarySupplier<StringPayload> binarySupplier = object.upload(anyStringPublisher(payload));

        subscribe(binarySupplier);
        return object;
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