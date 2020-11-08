package io.lette1394.mediaserver.storage.usecase.copy;

import io.lette1394.mediaserver.storage.domain.BinaryPath;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectFactory;
import io.lette1394.mediaserver.storage.domain.ObjectNotFoundException;
import io.lette1394.mediaserver.storage.domain.ObjectRepository;
import io.lette1394.mediaserver.storage.domain.Payload;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConditionalSoftCopy<BUFFER extends Payload> implements CopyStrategy<BUFFER> {

  private static final String TAG_COPYING_SOFT_COPIED                         = "copying.soft.copied";
  private static final String TAG_COPYING_SOFT_COPIED_SOURCE_REFERENCED_COUNT = "copying.soft.copied.source.referenced.count";
  private static final String TAG_COPYING_SOFT_COPIED_SOURCE_AREA             = "copying.soft.copied.source.area";
  private static final String TAG_COPYING_SOFT_COPIED_SOURCE_KEY              = "copying.soft.copied.source.key";

  private final long thresholdCountToSoftCopy;

  private final CopyStrategy<BUFFER> nextStrategy;
  private final ObjectFactory<BUFFER> objectFactory;
  private final ObjectRepository<BUFFER> objectRepository;

  @Override
  public CompletableFuture<Object<BUFFER>> execute(
    Object<BUFFER> sourceObject,
    Identifier targetIdentifier) {

    if (canHandle(sourceObject)) {
      return softCopy(sourceObject, targetIdentifier);
    }
    return nextStrategy.execute(sourceObject, targetIdentifier);
  }

  private boolean canHandle(Object<BUFFER> sourceObject) {
    final long referencedCount = sourceObject
      .getTag(TAG_COPYING_SOFT_COPIED_SOURCE_REFERENCED_COUNT)
      .asLongOrDefault(0L);

    return sourceObject.hasTag(TAG_COPYING_SOFT_COPIED) &&
      (referencedCount < thresholdCountToSoftCopy);
  }

  private CompletableFuture<Object<BUFFER>> softCopy(
    Object<BUFFER> sourceObject,
    Identifier targetIdentifier) {

    final Identifier sourceIdentifier = sourceObject.getIdentifier();
    final Object<BUFFER> targetObject = objectFactory.create(targetIdentifier);
    targetObject.addTag(TAG_COPYING_SOFT_COPIED);
    targetObject.addTag(TAG_COPYING_SOFT_COPIED_SOURCE_AREA, sourceIdentifier.getArea());
    targetObject.addTag(TAG_COPYING_SOFT_COPIED_SOURCE_KEY, sourceIdentifier.getKey());

    final long softCount = sourceObject
      .getTag(TAG_COPYING_SOFT_COPIED_SOURCE_REFERENCED_COUNT)
      .asLongOrDefault(0L);
    sourceObject.addTag(TAG_COPYING_SOFT_COPIED_SOURCE_REFERENCED_COUNT, softCount + 1);

    return CompletableFuture
      .allOf(
        objectRepository.save(sourceObject),
        objectRepository.save(targetObject))
      .thenApply(__ -> targetObject);
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

      delegate
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

      return null;
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
