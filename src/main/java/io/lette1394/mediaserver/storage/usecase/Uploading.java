package io.lette1394.mediaserver.storage.usecase;

import static io.lette1394.mediaserver.storage.domain.ObjectType.FULFILLED;
import static io.lette1394.mediaserver.storage.domain.ObjectType.PENDING;
import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.instanceOf;

import io.lette1394.mediaserver.storage.domain.BinaryPath;
import io.lette1394.mediaserver.storage.domain.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.BinaryPublisher;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectFactory;
import io.lette1394.mediaserver.storage.domain.ObjectNotFoundException;
import io.lette1394.mediaserver.storage.domain.ObjectRepository;
import io.lette1394.mediaserver.storage.domain.ObjectType;
import io.lette1394.mediaserver.storage.domain.Payload;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class Uploading<P extends Payload> {

  private final BinaryRepository<P> binaryRepository;
  private final ObjectRepository<P> objectRepository;

  private final ObjectFactory<P> objectFactory = new ObjectFactory<>();

  // 요구사항
  // 1. resume 업로드 되는 친구/아예 안되는 친구 효율적인 제어 흐름...
  //   - resume 업로드 설정이 되어있는 친구면 이미 올라온게 있는지 찔러보고 이어서 고고
  //   - 안되어있으면 그냥 무조건 덮어쓰기
  //   - 이 때 range 업로드면?
  // 2. 업로드 트랜잭션
  // 3. 기타 각종 정책
//


  public CompletableFuture<Object<P>> upload(Command<P> command) {
    return objectRepository
      .find(command.identifier)
      .handle(dispatch(command))
      .thenCompose(__ -> __);
  }

  private BiFunction<Object<P>, Throwable, CompletableFuture<Object<P>>> dispatch(
    Command<P> command) {
    final Identifier identifier = command.identifier;
    final BinaryPublisher<P> upstream = command.upstream;

    return (object, e) -> {
      if (isObjectExists(e)) {
        return Match(object)
          .of(
            Case($(is(FULFILLED)), () -> overwrite(object, upstream)),
            Case($(is(PENDING)), () -> append(object, upstream)));
      }
      return Match(e)
        .of(
          Case($(instanceOf(ObjectNotFoundException.class)), () -> create(identifier, upstream)),
          Case($(), () -> abortUpload(e)));
    };
  }

  private Predicate<Object<P>> is(ObjectType objectType) {
    return object -> object.is(objectType);
  }

  private CompletableFuture<Object<P>> append(Object<P> object,
    BinaryPublisher<P> upstream) {
    final BinaryPublisher<P> binary = object.upload(upstream);
    final BinaryPath binaryPath = binaryPath(object.getIdentifier());

    return binaryRepository.append(binaryPath, binary)
      .thenCompose(__ -> objectRepository.save(object));
  }

  private CompletableFuture<Object<P>> create(Identifier identifier,
    BinaryPublisher<P> upstream) {
    final Object<P> object = objectFactory.create(identifier);
    final BinaryPublisher<P> binaryPublisher = object.upload(upstream);
    final BinaryPath binaryPath = binaryPath(identifier);

    return binaryRepository.create(binaryPath, binaryPublisher)
      .thenCompose(__ -> objectRepository.save(object));
  }

  private CompletableFuture<Object<P>> overwrite(Object<P> object,
    BinaryPublisher<P> upstream) {
    final BinaryPublisher<P> binaryPublisher = object.upload(upstream);
    final BinaryPath binaryPath = binaryPath(object.getIdentifier());

    return binaryRepository.create(binaryPath, binaryPublisher)
      .thenCompose(__ -> objectRepository.save(object));
  }

  private CompletableFuture<Object<P>> abortUpload(Throwable e) {
    return CompletableFuture.failedFuture(e);
  }

  private BinaryPath binaryPath(Identifier identifier) {
    return BinaryPath.from(identifier);
  }

  private boolean isObjectExists(Throwable throwable) {
    return throwable == null;
  }

  @Value
  @Builder
  public static class Command<BUFFER extends Payload> {
    Identifier identifier;
    Map<String, String> tags;
    // TODO: CompletableFuture tag
    BinaryPublisher<BUFFER> upstream;

    public Command<BUFFER> with(BinaryPublisher<BUFFER> upstream) {
      return Command.<BUFFER>builder()
        .identifier(identifier)
        .tags(tags)
        .upstream(upstream)
        .build();
    }
  }
}
