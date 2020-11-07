package io.lette1394.mediaserver.storage.usecase;

import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectFactory;
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

  private final ObjectRepository<BUFFER> objectRepository;
  private final ObjectFactory<BUFFER> objectFactory;

  @Override
  public boolean matches(long softCopiedCount) {
    // TODO: fix condition
    return thresholdCountToSoftCopy >= softCopiedCount;
  }

  @Override
  public CompletableFuture<Object<BUFFER>> execute(Object<BUFFER> sourceObject, Identifier targetIdentifier) {
    final Identifier sourceIdentifier = sourceObject.getIdentifier();
    final Object<BUFFER> targetObject = objectFactory.create(targetIdentifier);
    targetObject.addTag(TAG_COPYING_SOFT_COPIED);
    targetObject.addTag(TAG_COPYING_SOFT_COPIED_SOURCE_AREA, sourceIdentifier.getArea());
    targetObject.addTag(TAG_COPYING_SOFT_COPIED_SOURCE_KEY, sourceIdentifier.getKey());

    final long softCount = sourceObject.getTag(TAG_COPYING_SOFT_COPIED_SOURCE_REFERENCED_COUNT).asLongOrDefault(0L);
    sourceObject.addTag(TAG_COPYING_SOFT_COPIED_SOURCE_REFERENCED_COUNT, softCount + 1);

    return CompletableFuture
      .allOf(
        objectRepository.save(sourceObject),
        objectRepository.save(targetObject))
      .thenApply(__ -> targetObject);
  }
}
