package io.lette1394.mediaserver.storage.domain;

import static io.lette1394.mediaserver.matchers.Matchers.causeIs;
import static io.lette1394.mediaserver.matchers.Matchers.commandIs;
import static io.lette1394.mediaserver.matchers.Matchers.typeIs;
import static io.lette1394.mediaserver.matchers.ObjectMatchers.events;
import static io.lette1394.mediaserver.matchers.ObjectMatchers.got;
import static io.lette1394.mediaserver.matchers.ObjectMatchers.hasSize;
import static io.lette1394.mediaserver.matchers.ObjectMatchers.hasType;
import static io.lette1394.mediaserver.storage.domain.BinaryPublisher.adapt;
import static io.lette1394.mediaserver.storage.domain.Command.UPLOAD;
import static io.lette1394.mediaserver.storage.domain.ObjectType.FULFILLED;
import static io.lette1394.mediaserver.storage.domain.ObjectType.PENDING;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.lette1394.mediaserver.common.Contracts;
import io.lette1394.mediaserver.storage.BrokenBinaryPublisher;
import io.lette1394.mediaserver.storage.BrokenBinaryPublisher.BrokenIOException;
import io.lette1394.mediaserver.storage.StringInMemoryRepository;
import io.lette1394.mediaserver.storage.domain.Events.UploadAborted;
import io.lette1394.mediaserver.storage.domain.Events.UploadRejected;
import io.lette1394.mediaserver.storage.domain.Events.Uploaded;
import io.lette1394.mediaserver.storage.domain.Events.UploadingTriggered;
import io.lette1394.mediaserver.storage.infrastructure.StringPayload;
import io.vavr.control.Try;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;

@DisplayName("Object")
class ObjectTest {
  private static final ObjectPolicy ALLOW = __ -> Try.success(null);
  private static final ObjectPolicy REJECT = __ -> Try.failure(new AlwaysRejectedTestException());

  private Object<StringPayload> anyObject() {
    return anyObject(__ -> Try.success(null), __ -> Try.success(null));
  }

  private Object<StringPayload> anyObjectWith(ObjectPolicy objectPolicy) {
    return anyObject(objectPolicy, __ -> Try.success(null));
  }

  private Object<StringPayload> anyObject(BinaryPolicy binaryPolicy) {
    return anyObject(__ -> Try.success(null), binaryPolicy);
  }

  private Object<StringPayload> anyObject(ObjectPolicy objectPolicy,
    BinaryPolicy binaryPolicy) {
    final BinaryRepository<StringPayload> binaryRepository = new StringInMemoryRepository();
    final ObjectFactory<StringPayload> objectFactory = new ObjectFactory<>(objectPolicy,
      binaryPolicy, binaryRepository);
    return objectFactory.create(anyIdentifier());
  }

  private Identifier anyIdentifier() {
    return new Identifier(randomAlphanumeric(10), randomAlphanumeric(10));
  }

  private Publisher<StringPayload> stringPublisher(String string) {
    return Flux.fromStream(Arrays.stream(string.split("")).map(StringPayload::new));
  }

  private <T extends Payload> void subscribe(BinaryPublisher<T> binaryPublisher) {
    Flux.from(binaryPublisher)
      .subscribe(__ -> {
      });
  }

  private BinaryPublisher<StringPayload> anyBinaries() {
    return adapt(stringPublisher("any string payload"));
  }

  private BinaryPublisher<StringPayload> anyBinariesWithLength(int length) {
    return adapt(stringPublisher(randomAlphanumeric(length)));
  }

  public static class AlwaysRejectedTestException extends RuntimeException {

  }

  @Nested
  @DisplayName("upload()")
  class Describe_Upload {
    @Nested
    @DisplayName("with pending_state")
    class Context_with_pending_state {
      @Test
      @DisplayName("")
      void pending() {
      }
    }

    @Nested
    @DisplayName("with legal policy")
    class Context_with_legal_policy {
      @Test
      @DisplayName("It emits successful events")
      void got_events() {
        final Object<StringPayload> object = anyObjectWith(ALLOW)
          .upload(anyBinaries())
          .join();
        assertThat(object, got(events(UploadingTriggered.class, Uploaded.class)));
      }

      @Test
      @DisplayName("It has FULFILLED type")
      void fulfilled() {
        final Object<StringPayload> object = anyObjectWith(ALLOW)
          .upload(anyBinaries())
          .join();
        assertThat(object, hasType(FULFILLED));
      }

      @Test
      @DisplayName("Its size is equals to payload length")
      void length() {
        final int payloadLength = 10;
        final Object<StringPayload> object = anyObjectWith(ALLOW)
          .upload(anyBinariesWithLength(payloadLength))
          .join();
        assertThat(object, hasSize(payloadLength));
      }
    }

    @Nested
    @DisplayName("with illegal policy")
    class Context_with_illegal_policy {
      @Test
      @DisplayName("it throws exception")
      void exception() {
        final Object<StringPayload> object = anyObjectWith(REJECT);
        final CompletionException wrapped = assertThrows(CompletionException.class,
          () -> object.upload(anyBinaries()).join());

        final Throwable exception = wrapped.getCause();
        assertThat(exception, typeIs(OperationCanceledException.class));
        assertThat(exception, causeIs(AlwaysRejectedTestException.class));
        assertThat(exception, commandIs(UPLOAD));
      }

      @Test
      @DisplayName("It emits rejected events")
      void events() {
        final Object<StringPayload> object = anyObjectWith(REJECT);
        assertThrows(CompletionException.class, () -> object.upload(anyBinaries()).join());
        assertThat(object, got(UploadRejected.class));
      }
    }

    @Nested
    @DisplayName("with illegal binaries")
    class Context_with_illegal_binaries {
      @Test
      @DisplayName("it throws exception")
      void it_throws_exception() {
        final Object<StringPayload> object = anyObjectWith(ALLOW);
        final CompletionException wrapped = assertThrows(CompletionException.class,
          () -> object.upload(brokenBinaries()).join());

        final Throwable exception = wrapped.getCause();
        assertThat(exception, typeIs(OperationCanceledException.class));
        assertThat(exception, causeIs(BrokenIOException.class));
        assertThat(exception, commandIs(UPLOAD));
      }

      @Test
      @DisplayName("It emits failure events")
      void It_emits_failure_events() {
        final Object<StringPayload> object = anyObjectWith(ALLOW);
        assertThrows(CompletionException.class, () -> object.upload(brokenBinaries()).join());
        assertThat(object, got(events(UploadingTriggered.class, UploadAborted.class)));
      }

      @Test
      @DisplayName("It has PENDING type")
      void It_has_PENDING_state() {
        final Object<StringPayload> object = anyObjectWith(ALLOW);
        assertThrows(CompletionException.class, () -> object.upload(brokenBinaries()).join());
        assertThat(object, hasType(PENDING));
      }

      @Test
      @DisplayName("Its size is equal to last uploaded length")
      void Its_size_is_equal_to_last_uploaded_length() {
        final Object<StringPayload> object = anyObjectWith(ALLOW);
        final CompletionException wrapped = assertThrows(CompletionException.class,
          () -> object.upload(brokenBinaries()).join());

        final Throwable exception = wrapped.getCause();
        assertThat(exception, typeIs(OperationCanceledException.class));
        assertThat(exception, causeIs(BrokenIOException.class));
        assertThat(object, hasSize(unwrap(exception).getExceptionAt()));
      }

      private BrokenIOException unwrap(Throwable throwable) {
        return ((BrokenIOException) throwable.getCause());
      }

      private BinaryPublisher<StringPayload> brokenBinaries() {
        final String payload = "broken binaries payload";
        final int binaryLength = payload.length();
        final int exceptionAt = RandomUtils.nextInt(0, binaryLength);
        Contracts.require(exceptionAt < binaryLength, "exceptionAt < binaryLength");

        final Publisher<StringPayload> publisher = stringPublisher(payload);
        return adapt(new BrokenBinaryPublisher<>(exceptionAt, new BinaryPublisher<>() {
          @Override
          public void subscribe(Subscriber<? super StringPayload> s) {
            publisher.subscribe(s);
          }

          @Override
          public Optional<Long> length() {
            return Optional.of((long) payload.length());
          }
        }));
      }
    }
  }
}