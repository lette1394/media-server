package io.lette1394.mediaserver.storage.usecase;

import static io.lette1394.mediaserver.matchers.ObjectMatchers.got;
import static io.lette1394.mediaserver.matchers.ObjectMatchers.hasProgressingSize;
import static io.lette1394.mediaserver.matchers.ObjectMatchers.hasSize;
import static io.lette1394.mediaserver.matchers.ObjectMatchers.hasType;
import static io.lette1394.mediaserver.storage.domain.BinaryPublisher.adapt;
import static io.lette1394.mediaserver.storage.domain.ObjectType.FULFILLED;
import static io.lette1394.mediaserver.storage.domain.ObjectType.PENDING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.lette1394.mediaserver.storage.BrokenBinaryPublisher;
import io.lette1394.mediaserver.storage.ByteInMemoryRepository;
import io.lette1394.mediaserver.storage.domain.BinaryPath;
import io.lette1394.mediaserver.storage.domain.BinaryPublisher;
import io.lette1394.mediaserver.storage.domain.BinarySnapshot;
import io.lette1394.mediaserver.storage.domain.Events.Uploaded;
import io.lette1394.mediaserver.storage.domain.Events.UploadingTriggered;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectFactory;
import io.lette1394.mediaserver.storage.domain.ObjectSnapshot;
import io.lette1394.mediaserver.storage.domain.ObjectType;
import io.lette1394.mediaserver.storage.domain.Tags;
import io.lette1394.mediaserver.storage.infrastructure.BytePayload;
import io.lette1394.mediaserver.storage.usecase.Uploading.Command;
import io.vavr.control.Try;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;

class UploadingTest {
  Uploading<BytePayload> uploading;
  ByteInMemoryRepository repository = new ByteInMemoryRepository();
  Identifier pendingIdentifier = new Identifier("pending", "object");
  String initialPayload = "payload";
  String appendedPayload = " appended";
  long brokenAt = 3;

  @BeforeEach
  void beforeEach() {
    repository.addObject(pendingObject(pendingIdentifier));
    repository.addBinary(BinaryPath.from(pendingIdentifier), initialPayload.getBytes());

    uploading = new Uploading<>(new ObjectFactory<>(repository), repository);
  }

  @Test
  void resume() {
    final Object<BytePayload> object = uploading
      .upload(Command.<BytePayload>builder()
        .identifier(pendingIdentifier)
        .upstream(adapt(publisher(appendedPayload)))
        .tags(CompletableFuture.completedFuture(Tags.empty()))
        .build())
      .join();

    final String appendedPayload = getPayload(pendingIdentifier);

    assertThat(appendedPayload, is("payload appended"));
    assertThat(object, got(List.of(UploadingTriggered.class, Uploaded.class)));
    assertThat(object, hasType(FULFILLED));
    assertThat(object, hasSize(fullSize()));
    assertThat(object, hasProgressingSize(0L));
  }

  @Test
  void pendingToPendingWhenBroken() {
    assertThrows(CompletionException.class,
      () -> uploading
        .upload(Command.<BytePayload>builder()
          .identifier(pendingIdentifier)
          .upstream(adapt(brokenPublisherAt(appendedPayload, brokenAt)))
          .build())
        .join());

    final Object<BytePayload> object = getObject(pendingIdentifier);
    assertThat(getPayload(pendingIdentifier), is("payload ap"));
    assertThat(object, hasType(PENDING));
    assertThat(object, hasSize(brokenSize()));
    assertThat(object, hasProgressingSize(0L));
  }

  private Object<BytePayload> pendingObject(Identifier identifier) {
    return Object.<BytePayload>builder()
      .identifier(identifier)
      .binaryPath(BinaryPath.from(identifier))
      .objectSnapshot(ObjectSnapshot.byObjectType(
        ObjectType.PENDING,
        initialPayload.getBytes().length))
      .binarySnapshot(BinarySnapshot.initial())
      .binaryPolicy(binarySnapshot -> Try.success(null))
      .objectPolicy(snapshot -> Try.success(null))
      .binaryRepository(repository)
      .tags(Tags.empty())
      .build();
  }

  private Publisher<BytePayload> publisher(String payload) {
    return Flux.fromStream(payload.chars().mapToObj(ch -> new BytePayload(ch)));
  }

  private Publisher<BytePayload> brokenPublisherAt(String payload, long exceptionAt) {
    final Publisher<BytePayload> publisher = Flux
      .fromStream(payload.chars().mapToObj(ch -> new BytePayload(ch)));

    return new BrokenBinaryPublisher<>(exceptionAt, new BinaryPublisher<>() {
      @Override
      public void subscribe(Subscriber<? super BytePayload> s) {
        publisher.subscribe(s);
      }

      @Override
      public Optional<Long> length() {
        return Optional.of((long) payload.getBytes().length);
      }
    });
  }

  private String getPayload(Identifier identifier) {
    return new String(repository.getBinary(BinaryPath.from(identifier)));
  }

  private Object<BytePayload> getObject(Identifier identifier) {
    return repository.getObject(identifier);
  }

  private long fullSize() {
    return initialPayload.getBytes().length + appendedPayload.getBytes().length;
  }

  private long brokenSize() {
    return initialPayload.getBytes().length + brokenAt;
  }
}