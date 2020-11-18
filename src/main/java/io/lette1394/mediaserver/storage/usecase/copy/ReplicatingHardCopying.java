package io.lette1394.mediaserver.storage.usecase.copy;

import static java.util.concurrent.CompletableFuture.completedFuture;

import io.lette1394.mediaserver.storage.domain.DelegatingObjectRepository;
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
public class ReplicatingHardCopying<B extends Payload> implements CopyStrategy<B> {

  private static final String TAG_COPYING_REPLICATED               = "copying.replicated";
  private static final String TAG_COPYING_REPLICATED_REDIRECT_AREA = "copying.replicated.redirect.area";
  private static final String TAG_COPYING_REPLICATED_REDIRECT_KEY  = "copying.replicated.redirect.key";

  private final CopyStrategy<B> hardCopying;
  private final ObjectRepository<B> objectRepository;

  @Override
  public CompletableFuture<Object<B>> execute(
    Object<B> sourceObject,
    Identifier targetIdentifier) {

    return hardCopying.execute(sourceObject, targetIdentifier)
      .thenCompose(markReplicated(sourceObject));
  }

  private Function<Object<B>, CompletionStage<Object<B>>> markReplicated(
    Object<B> sourceObject) {

    return copiedObject -> {
      final Identifier copiedIdentifier = copiedObject.getIdentifier();
      sourceObject.addTag(TAG_COPYING_REPLICATED);
      sourceObject.addTag(TAG_COPYING_REPLICATED_REDIRECT_AREA, copiedIdentifier.getArea());
      sourceObject.addTag(TAG_COPYING_REPLICATED_REDIRECT_KEY, copiedIdentifier.getKey());

      return objectRepository.save(sourceObject)
        .thenApply(__ -> copiedObject);
    };
  }

  public static class ReplicaFollowingObjectRepository<P extends Payload>
    extends DelegatingObjectRepository<P> {

    public ReplicaFollowingObjectRepository(ObjectRepository<P> delegate) {
      super(delegate);
    }

    @Override
    public CompletableFuture<Object<P>> find(Identifier identifier)
      throws ObjectNotFoundException {

      return delegate
        .find(identifier)
        .thenCompose(maybeReplicatedObject -> {
          if (maybeReplicatedObject.hasTag(TAG_COPYING_REPLICATED)) {
            return followingReplica(maybeReplicatedObject);
          }
          return completedFuture(maybeReplicatedObject);
        });
    }

    private CompletionStage<Object<P>> followingReplica(Object<P> maybeReplicatedObject) {
      final String targetArea = maybeReplicatedObject
        .getTag(TAG_COPYING_REPLICATED_REDIRECT_AREA)
        .asString();
      final String targetKey = maybeReplicatedObject
        .getTag(TAG_COPYING_REPLICATED_REDIRECT_KEY)
        .asString();
      return this.find(new Identifier(targetArea, targetKey));
    }
  }
}
