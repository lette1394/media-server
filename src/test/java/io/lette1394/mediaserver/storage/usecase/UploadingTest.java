package io.lette1394.mediaserver.storage.usecase;

import static io.lette1394.mediaserver.matchers.ObjectMatchers.got;
import static io.lette1394.mediaserver.matchers.ObjectMatchers.hasLength;
import static io.lette1394.mediaserver.matchers.ObjectMatchers.hasType;
import static io.lette1394.mediaserver.storage.domain.ObjectType.FULFILLED;
import static io.lette1394.mediaserver.storage.domain.ObjectType.PENDING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.lette1394.mediaserver.storage.BrokenBinarySupplier;
import io.lette1394.mediaserver.storage.ByteInMemoryRepository;
import io.lette1394.mediaserver.storage.domain.BinaryPath;
import io.lette1394.mediaserver.storage.domain.BinarySnapshot;
import io.lette1394.mediaserver.storage.domain.Events.Uploaded;
import io.lette1394.mediaserver.storage.domain.Events.UploadingTriggered;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.LengthAwareBinarySupplier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectSnapshot;
import io.lette1394.mediaserver.storage.domain.ObjectType;
import io.lette1394.mediaserver.storage.infrastructure.BytePayload;
import io.lette1394.mediaserver.storage.usecase.Uploading.Command;
import io.vavr.control.Try;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
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
    repository.objectHolder.put(pendingIdentifier, pendingObject(pendingIdentifier));
    repository.binaryHolder.put(BinaryPath.from(pendingIdentifier).asString(), payload(initialPayload));

    uploading = new Uploading<>(repository, repository);
  }

  @Test
  void resume() {
    final Object<BytePayload> object = uploading
      .upload(Command.<BytePayload>builder()
        .identifier(pendingIdentifier)
        .upstream(publisher(appendedPayload))
        .build())
      .join();

    final String appendedPayload = getPayload(pendingIdentifier);

    assertThat(appendedPayload, is("payload appended"));
    assertThat(object, got(List.of(UploadingTriggered.class, Uploaded.class)));
    assertThat(object, hasType(FULFILLED));
  }

  @Test
  void pendingToPendingWhenBroken() {
    assertThrows(CompletionException.class,
      () -> uploading
        .upload(Command.<BytePayload>builder()
          .identifier(pendingIdentifier)
          .upstream(brokenPublisherAt(appendedPayload, brokenAt))
          .build())
        .join());

    assertThat(getPayload(pendingIdentifier), is("payload ap"));
    assertThat(getObject(pendingIdentifier), hasType(PENDING));
    assertThat(getObject(pendingIdentifier), hasLength(initialPayload.getBytes().length + brokenAt));
  }

  private Object<BytePayload> pendingObject(Identifier identifier) {
    return Object.<BytePayload>builder()
      .identifier(identifier)
      .objectSnapshot(ObjectSnapshot
        .builder()
        .objectType(ObjectType.PENDING)
        .size(initialPayload.getBytes().length)
        .build())
      .binarySnapshot(BinarySnapshot.initial())
      .binaryPolicy(binarySnapshot -> Try.success(null))
      .objectPolicy(snapshot -> Try.success(null))
      .binaryRepository(repository)
      .build();
  }

  private ByteArrayOutputStream payload(String payload) {
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    outputStream.writeBytes(payload.getBytes());
    return outputStream;
  }

  private Publisher<BytePayload> publisher(String payload) {
    return Flux.fromStream(payload.chars().mapToObj(ch -> new BytePayload(ch)));
  }

  private Publisher<BytePayload> brokenPublisherAt(String payload, long at) {
    final Publisher<BytePayload> publisher = Flux
      .fromStream(payload.chars().mapToObj(ch -> new BytePayload(ch)));

    return new BrokenBinarySupplier<BytePayload>(new LengthAwareBinarySupplier<>() {
      @Override
      public long getLength() {
        return payload.getBytes().length;
      }

      @Override
      public Publisher<BytePayload> getAsync() {
        return publisher;
      }
    }, at)
      .getAsync();
  }

  private String getPayload(Identifier identifier) {
    final ByteArrayOutputStream outputStream = repository.binaryHolder
      .get(BinaryPath.from(identifier).asString());

    return new String(outputStream.toByteArray());
  }

  private Object<BytePayload> getObject(Identifier identifier) {
    return repository.objectHolder.get(identifier);
  }
}