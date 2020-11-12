package io.lette1394.mediaserver.storage.usecase.copy;

import io.lette1394.mediaserver.storage.domain.BinaryPath;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.NoOperationSubscriber;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectFactory;
import io.lette1394.mediaserver.storage.domain.ObjectNotFoundException;
import io.lette1394.mediaserver.storage.domain.ObjectRepository;
import io.lette1394.mediaserver.storage.domain.Payload;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class SoftCopying<B extends Payload> implements CopyStrategy<B> {

  static final String TAG_COPYING_SOFT_COPIED                         = "copying.soft.copied";
  static final String TAG_COPYING_SOFT_COPIED_SOURCE_REFERENCED_COUNT = "copying.soft.copied.source.referenced.count";
  static final String TAG_COPYING_SOFT_COPIED_SOURCE_AREA             = "copying.soft.copied.source.area";
  static final String TAG_COPYING_SOFT_COPIED_SOURCE_KEY              = "copying.soft.copied.source.key";

  private final ObjectFactory<B> objectFactory;
  private final ObjectRepository<B> objectRepository;

  @Override
  public CompletableFuture<Object<B>> execute(
    Object<B> sourceObject,
    Identifier targetIdentifier) {

    final Object<B> targetObject = objectFactory.create(targetIdentifier);

    markSoftCopied(sourceObject, targetObject);
    pretendingToCopy(sourceObject, targetObject);
    increaseReferencedCount(sourceObject);

    // TODO: atomic update
    // TODO: transaction
    return CompletableFuture
      .allOf(
        objectRepository.save(sourceObject),
        objectRepository.save(targetObject))
      .thenApply(__ -> targetObject);
  }

  private void markSoftCopied(Object<B> sourceObject, Object<B> targetObject) {
    final Identifier sourceIdentifier = sourceObject.getIdentifier();
    targetObject.addTag(TAG_COPYING_SOFT_COPIED);
    targetObject.addTag(TAG_COPYING_SOFT_COPIED_SOURCE_AREA, sourceIdentifier.getArea());
    targetObject.addTag(TAG_COPYING_SOFT_COPIED_SOURCE_KEY, sourceIdentifier.getKey());
  }

  @SuppressWarnings("unchecked")
  private void pretendingToCopy(Object<B> sourceObject, Object<B> targetObject) {
    final Payload notifyPayload = () -> sourceObject.getSize();
    targetObject
      .copyFrom(() -> Mono.just((B) notifyPayload))
      .publisher().subscribe(new NoOperationSubscriber<>() {
      @Override
      public void onSubscribe(Subscription s) {
        s.request(1L);
      }
    });
  }

  private void increaseReferencedCount(Object<B> sourceObject) {
    final long softCount = sourceObject
      .getTag(TAG_COPYING_SOFT_COPIED_SOURCE_REFERENCED_COUNT)
      .asLongOrDefault(0L);
    sourceObject.addTag(TAG_COPYING_SOFT_COPIED_SOURCE_REFERENCED_COUNT, softCount + 1);
  }

  @RequiredArgsConstructor
  public static class SoftCopyFollowingObjectRepository<BUFFER extends Payload>
    implements ObjectRepository<BUFFER> {

    private final ObjectRepository<BUFFER> delegate;

    @Override
    public CompletableFuture<Boolean> exists(Identifier identifier) {
      return delegate.exists(identifier);
    }

    @Override
    public CompletableFuture<Object<BUFFER>> find(Identifier identifier)
      throws ObjectNotFoundException {

      // TODO: recursive
      return delegate
        .find(identifier)
        .thenApply(maybeSoftCopiedObject -> {
          if (maybeSoftCopiedObject.hasTag(TAG_COPYING_SOFT_COPIED)) {
            final String sourceArea = maybeSoftCopiedObject
              .getTag(TAG_COPYING_SOFT_COPIED_SOURCE_AREA)
              .asString();
            final String sourceKey = maybeSoftCopiedObject
              .getTag(TAG_COPYING_SOFT_COPIED_SOURCE_KEY)
              .asString();
            return maybeSoftCopiedObject.with(BinaryPath.from(sourceArea, sourceKey));
          }
          return maybeSoftCopiedObject;
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
