package io.lette1394.mediaserver.storage.usecase.copy;

import static java.util.concurrent.CompletableFuture.completedFuture;

import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectNotFoundException;
import io.lette1394.mediaserver.storage.domain.ObjectRepository;
import io.lette1394.mediaserver.storage.domain.Payload;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReplicatingHardCopying<BUFFER extends Payload> implements CopyStrategy<BUFFER> {

  private static final String TAG_COPYING_REPLICATED               = "copying.replicated";
  private static final String TAG_COPYING_REPLICATED_REDIRECT_AREA = "copying.replicated.redirect.area";
  private static final String TAG_COPYING_REPLICATED_REDIRECT_KEY  = "copying.replicated.redirect.key";

  private final CopyStrategy<BUFFER> hardCopying;
  private final ObjectRepository<BUFFER> objectRepository;

  @Override
  public CompletableFuture<Object<BUFFER>> execute(
    Object<BUFFER> sourceObject,
    Identifier targetIdentifier) {

    // TODO: 어... 그러니까
    //  실제로 하는 거는 hard copy 인데
    //  그 동작을 분기하는 기준은 soft copy 라고?
    //  후... matcher를 따로 빼야하나?
    return hardCopying.execute(sourceObject, targetIdentifier)
      .thenCompose(markReplicated(sourceObject));
  }

  private Function<Object<BUFFER>, CompletionStage<Object<BUFFER>>> markReplicated(
    Object<BUFFER> sourceObject) {

    return copiedObject -> {
      final Identifier copiedIdentifier = copiedObject.getIdentifier();
      sourceObject.addTag(TAG_COPYING_REPLICATED);
      sourceObject.addTag(TAG_COPYING_REPLICATED_REDIRECT_AREA, copiedIdentifier.getArea());
      sourceObject.addTag(TAG_COPYING_REPLICATED_REDIRECT_KEY, copiedIdentifier.getKey());

      return objectRepository.save(sourceObject)
        .thenApply(__ -> copiedObject);
    };
  }

  // TODO: 이걸 downloading usecase에 합성 필요
  @RequiredArgsConstructor
  public static class ReplicaFollowingObjectRepository<BUFFER extends Payload>
    implements ObjectRepository<BUFFER> {

    private final ObjectRepository<BUFFER> delegate;

    @Override
    public CompletableFuture<Boolean> exists(Identifier identifier) {
      return delegate.exists(identifier);
    }

    @Override
    public CompletableFuture<Object<BUFFER>> find(Identifier identifier)
      throws ObjectNotFoundException {

      return delegate
        .find(identifier)
        .thenCompose(maybeReplicatedObject -> {

          // TODO: NEED RECURSIVE FOLLOWING
          if (!maybeReplicatedObject.hasTag(TAG_COPYING_REPLICATED)) {
            return completedFuture(maybeReplicatedObject);
          }

          final String targetArea = maybeReplicatedObject
            .getTag(TAG_COPYING_REPLICATED_REDIRECT_AREA)
            .asString();
          final String targetKey = maybeReplicatedObject
            .getTag(TAG_COPYING_REPLICATED_REDIRECT_KEY)
            .asString();
          return delegate.find(new Identifier(targetArea, targetKey));
        });
    }

    @Override
    public CompletableFuture<Object<BUFFER>> save(Object<BUFFER> object) {
      return delegate.save(object);
    }

    @Override
    public CompletableFuture<Void> delete(Identifier identifier) {
      return delegate.delete(identifier);
    }
  }
}
