package io.lette1394.mediaserver.storage.usecase.copy;

import static io.lette1394.mediaserver.matchers.Matchers.typeIs;
import static io.lette1394.mediaserver.matchers.ObjectMatchers.events;
import static io.lette1394.mediaserver.matchers.ObjectMatchers.got;
import static io.lette1394.mediaserver.matchers.ObjectMatchers.hasSize;
import static io.lette1394.mediaserver.matchers.ObjectMatchers.hasType;
import static io.lette1394.mediaserver.utils.TestUtils.ALLOW_BINARY_POLICY;
import static io.lette1394.mediaserver.utils.TestUtils.ALLOW_OBJECT_POLICY;
import static io.lette1394.mediaserver.utils.TestUtils.anyIdentifier;
import static io.lette1394.mediaserver.utils.TestUtils.anyTimeStamp;
import static io.lette1394.mediaserver.utils.TestUtils.randomIdentifier;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.lette1394.mediaserver.storage.InMemoryStorage;
import io.lette1394.mediaserver.storage.StringInMemoryRepository;
import io.lette1394.mediaserver.storage.domain.BinaryPath;
import io.lette1394.mediaserver.storage.domain.BinarySnapshot;
import io.lette1394.mediaserver.storage.domain.Events.Copied;
import io.lette1394.mediaserver.storage.domain.Events.CopyingTriggered;
import io.lette1394.mediaserver.storage.domain.Events.Downloaded;
import io.lette1394.mediaserver.storage.domain.Events.DownloadingTriggered;
import io.lette1394.mediaserver.storage.domain.Events.Uploaded;
import io.lette1394.mediaserver.storage.domain.Events.UploadingTriggered;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectFactory;
import io.lette1394.mediaserver.storage.domain.ObjectNotFoundException;
import io.lette1394.mediaserver.storage.domain.ObjectSnapshot;
import io.lette1394.mediaserver.storage.domain.ObjectType;
import io.lette1394.mediaserver.storage.domain.Tags;
import io.lette1394.mediaserver.storage.infrastructure.StringPayload;
import io.lette1394.mediaserver.storage.usecase.copy.Copying.Command;
import io.lette1394.mediaserver.storage.usecase.copy.Copying.CopyMode;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Copying")
class CopyingTest {
  InMemoryStorage<StringPayload> memory;
  ObjectFactory<StringPayload> objectFactory;
  Object<StringPayload> sourceObject;

  @BeforeEach
  void beforeEach() {
    memory = new StringInMemoryRepository();
    objectFactory = new ObjectFactory<>(memory);

    final String payload = "source object payload @@@";
    final Identifier identifier = anyIdentifier();
    sourceObject = Object.<StringPayload>builder()
      .identifier(identifier)
      .binaryPath(BinaryPath.from(identifier))
      .binaryRepository(memory)
      .tags(Tags.empty())
      .objectPolicy(ALLOW_OBJECT_POLICY)
      .binaryPolicy(ALLOW_BINARY_POLICY)
      .objectSnapshot(ObjectSnapshot.byObjectType(ObjectType.FULFILLED, payload.length()))
      .binarySnapshot(BinarySnapshot.initial())
      .timeStamp(anyTimeStamp())
      .build();

    memory.addObject(sourceObject);
    memory.addBinary(BinaryPath.from(sourceObject.getIdentifier()), payload.getBytes());
  }

  private Command hardMode(Identifier source, Identifier target) {
    return Command.builder()
      .mode(CopyMode.HARD)
      .from(source)
      .to(target)
      .build();
  }

  private Identifier notFoundIdentifier() {
    final Identifier identifier = sourceObject.getIdentifier();
    return new Identifier("not-found-id-" + identifier.getArea(),
      "not-found-id-" + identifier.getKey());
  }

  private Copying<StringPayload> subject() {
    final CopyStrategy<StringPayload> hardCopying = new HardCopying<>(objectFactory, memory);
    final CopyStrategy<StringPayload> softCopying = new SoftCopying<>(objectFactory, memory);
    final CopyStrategy<StringPayload> replicatingHardCopying = new ReplicatingHardCopying<>(
      hardCopying, memory);

    return new Copying<StringPayload>(memory, hardCopying, softCopying, replicatingHardCopying);
  }

  @Nested
  @DisplayName("copy()")
  class Copy {

    // TODO: source object를 찾을 수 없을 때
    @Nested
    @DisplayName("with not found identifier")
    class with_not_found_identifier {
      @Test
      @DisplayName("it throws not found exception")
      void test1() {
        final Identifier sourceId = notFoundIdentifier();
        final Identifier targetId = anyIdentifier();
        final CompletionException wrapped = assertThrows(CompletionException.class,
          () -> subject().copy(hardMode(sourceId, targetId)).join());
        final Throwable throwable = wrapped.getCause();

        assertThat(throwable, typeIs(ObjectNotFoundException.class));
      }
    }

    @Nested
    @DisplayName("with hard copy mode")
    class with_hard_copy_mode {
      @Test
      @DisplayName("success blah blah")
      void test1() {
        final Identifier sourceId = sourceObject.getIdentifier();
        final Identifier targetId = randomIdentifier();
        final Object<StringPayload> copiedObject = subject()
          .copy(hardMode(sourceId, targetId))
          .join();

        assertThat(copiedObject, hasType(ObjectType.FULFILLED));
        assertThat(copiedObject, hasSize(sourceObject.getSize()));
        assertThat(sourceObject, got(events(DownloadingTriggered.class, Downloaded.class)));
        assertThat(copiedObject, got(events(UploadingTriggered.class, Uploaded.class)));

        // TODO: 엄...
        //  이거 이러면 어떻게 되는거지
        //  object 에 copy event가 없다라...
        //  그러면 음...
        //  .
        //  .
        //  .
        //  1. 애초에 event로 뭘 할 수 있는지를 생각해보자
        //  2. 다른 aggregate이 존재하는건가?
        //  3. !!!invariants 를 기준으로 생각하자!!!
        //    storage object invariants 는 뭐가 있지...?
        //
      }
    }
  }
}