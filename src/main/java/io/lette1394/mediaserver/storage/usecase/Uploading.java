package io.lette1394.mediaserver.storage.usecase;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.instanceOf;
import static java.util.Objects.isNull;

import io.lette1394.mediaserver.storage.domain.BinaryPublisher;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectFactory;
import io.lette1394.mediaserver.storage.domain.ObjectNotFoundException;
import io.lette1394.mediaserver.storage.domain.ObjectRepository;
import io.lette1394.mediaserver.storage.domain.ObjectType;
import io.lette1394.mediaserver.storage.domain.Payload;
import io.lette1394.mediaserver.storage.domain.Tags;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

// TODO: ObjectService 클래스 만들고
//  objectService.upload();
//  objectService.overwrite();
//  objectService.append();
//  objectService.hardCopy();
//  objectService.softCopy();
//  objectService.replicatingHardCopy();
//  이렇게 하는건가?
//  .
//  .
//  .
//  그러면 이런 usecase class는 어떻게 쓸 수 있을까?
//  => 아마도 "진짜" usecase를 나타내야 할 것이다.
@RequiredArgsConstructor
public class Uploading<P extends Payload> {

  private final ObjectFactory<P> objectFactory;
  private final ObjectRepository<P> objectRepository;

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
      .thenCompose(unwrap())
      .thenCombine(command.tags, addAllTags())
      .thenCompose(saveObject());
  }

  private BiFunction<Object<P>, Throwable, CompletableFuture<Object<P>>> dispatch(Command<P> command) {
    final Identifier identifier = command.identifier;
    final BinaryPublisher<P> upstream = command.upstream;

    return (object, e) -> {
      if (isObjectExist(e)) {
        return Match(object).of(
          // FIXME (jaeeun) 2020-11-23:
          //  여기서... 엄... 서비스 별 정책이 달라진다는거지?
          //  어떤 애는 이어올리기를 지원 안하니까 여기가 무조건 object.create() 일꺼고?
          Case($(o -> o.is(ObjectType.PENDING)), () -> object.append(upstream)),
          Case($(o -> o.is(ObjectType.FULFILLED)), () -> object.upload(upstream)),
          Case($(), () -> abortUpload(new IllegalStateException("illegal state"))));
      }
      return Match(e).of(
        Case($(instanceOf(ObjectNotFoundException.class)), () -> createNewObject(identifier, upstream)),
        Case($(), () -> abortUpload(e)));
    };
  }

  private boolean isObjectExist(Throwable throwable) {
    return isNull(throwable);
  }

  private BiFunction<Object<P>, Tags, Object<P>> addAllTags() {
    return (object, tags) -> {
      object.addAllTag(tags);
      return object;
    };
  }

  private Function<Object<P>, CompletionStage<Object<P>>> saveObject() {
    return object -> objectRepository.save(object);
  }

  private CompletableFuture<Object<P>> createNewObject(Identifier identifier,
    BinaryPublisher<P> upstream) {
    return objectFactory.create(identifier).upload(upstream);
  }

  private CompletableFuture<Object<P>> abortUpload(Throwable e) {
    return CompletableFuture.failedFuture(e);
  }

  private Function<CompletableFuture<Object<P>>, CompletionStage<Object<P>>> unwrap() {
    return __ -> __;
  }

  @Value
  @Builder
  public static class Command<P extends Payload> {

    Identifier identifier;
    @Builder.Default // TODO: fix naive impl
      CompletableFuture<Tags> tags = CompletableFuture.completedFuture(Tags.empty());
    BinaryPublisher<P> upstream;

    public Command<P> with(BinaryPublisher<P> upstream) {
      return Command.<P>builder()
        .identifier(identifier)
        .tags(tags)
        .upstream(upstream)
        .build();
    }

    public Command<P> with(CompletableFuture<Tags> tags) {
      return Command.<P>builder()
        .identifier(identifier)
        .tags(tags)
        .upstream(upstream)
        .build();
    }
  }
}
