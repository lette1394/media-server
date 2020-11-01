package io.lette1394.mediaserver.storage.usecase;

import static io.lette1394.mediaserver.storage.domain.ObjectType.FULFILLED;
import static io.lette1394.mediaserver.storage.domain.ObjectType.PENDING;
import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.instanceOf;
import static java.util.Objects.isNull;

import io.lette1394.mediaserver.storage.domain.BinaryPath;
import io.lette1394.mediaserver.storage.domain.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.BinarySupplier;
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
import org.reactivestreams.Publisher;

@RequiredArgsConstructor
public class Uploading<BUFFER extends Payload> {
  private final BinaryRepository<BUFFER> binaryRepository;
  private final ObjectRepository<BUFFER> objectRepository;

  private final ObjectFactory<BUFFER> objectFactory = new ObjectFactory<>();

  // 요구사항
  // 1. resume 업로드 되는 친구/아예 안되는 친구 효율적인 제어 흐름...
  //   - resume 업로드 설정이 되어있는 친구면 이미 올라온게 있는지 찔러보고 이어서 고고
  //   - 안되어있으면 그냥 무조건 덮어쓰기
  //   - 이 때 range 업로드면?
  // 2. 업로드 트랜잭션
  // 3. 기타 각종 정책
//


  public CompletableFuture<Object<BUFFER>> upload(Command<BUFFER> command) {
    return objectRepository
      .find(command.identifier)
      .handle(dispatch(command))
      .thenCompose(__ -> __);
  }

  private BiFunction<Object<BUFFER>, Throwable, CompletableFuture<Object<BUFFER>>> dispatch(
    Command<BUFFER> command) {
    final Identifier identifier = command.identifier;
    final Publisher<BUFFER> upstream = command.upstream;

    return (object, e) -> {
      if (isNull(e)) {
        return Match(object)
          .of(
            Case($(is(FULFILLED)), () -> overwrite(object, upstream)),
            Case($(is(PENDING)), () -> append(object, upstream)))
          .thenApply(__ -> object);
      }
      return Match(e)
        .of(
          Case($(instanceOf(ObjectNotFoundException.class)), () -> create(identifier, upstream)),
          Case($(), () -> abortUpload(e)))
        .thenApply(__ -> object);
    };
  }

  private Predicate<Object<BUFFER>> is(ObjectType objectType) {
    return object -> object.is(objectType);
  }

  private CompletableFuture<Void> append(Object<BUFFER> object, Publisher<BUFFER> upstream) {
    final BinarySupplier<BUFFER> binary = object.upload(upstream);
    final BinaryPath binaryPath = binaryPath(object.getIdentifier());

    return binaryRepository.append(binaryPath, binary)
      .thenAccept(__ -> objectRepository.save(object));
  }

  private CompletableFuture<Void> create(Identifier identifier, Publisher<BUFFER> upstream) {
    final Object<BUFFER> object = objectFactory.create(identifier);
    final BinarySupplier<BUFFER> binarySupplier = object.upload(upstream);
    final BinaryPath binaryPath = binaryPath(identifier);

    return binaryRepository.create(binaryPath, binarySupplier)
      .thenAccept(__ -> objectRepository.save(object));
  }

  private CompletableFuture<Void> overwrite(Object<BUFFER> object, Publisher<BUFFER> upstream) {
    final BinarySupplier<BUFFER> binarySupplier = object.upload(upstream);
    final BinaryPath binaryPath = binaryPath(object.getIdentifier());

    return binaryRepository.create(binaryPath, binarySupplier)
      .thenAccept(__ -> objectRepository.save(object));
  }

  private CompletableFuture<Void> abortUpload(Throwable e) {
    return CompletableFuture.failedFuture(e);
  }

  private BinaryPath binaryPath(Identifier identifier) {
    return BinaryPath.from(identifier);
  }

  @Value
  @Builder
  public static class Command<BUFFER extends Payload> {
    Identifier identifier;
    Publisher<BUFFER> upstream;
    Map<String, String> tags;
  }
}
