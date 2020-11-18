package io.lette1394.mediaserver.storage.usecase.copy;

import static io.lette1394.mediaserver.storage.domain.BinaryPublisher.adapt;

import io.lette1394.mediaserver.storage.domain.BinaryPath;
import io.lette1394.mediaserver.storage.domain.BinaryPublisher;
import io.lette1394.mediaserver.storage.domain.BinaryRepository;
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

public class SoftCopying<P extends Payload> implements CopyStrategy<P> {

  static final String TAG_COPYING_SOFT_COPIED = "copying.soft.copied";
  static final String TAG_COPYING_SOFT_COPIED_SOURCE_REFERENCED_COUNT = "copying.soft.copied.source.referenced.count";
  static final String TAG_COPYING_SOFT_COPIED_SOURCE_AREA = "copying.soft.copied.source.area";
  static final String TAG_COPYING_SOFT_COPIED_SOURCE_KEY = "copying.soft.copied.source.key";

  private final ObjectFactory<P> objectFactory;
  private final ObjectRepository<P> objectRepository;

  public SoftCopying(ObjectFactory<P> objectFactory,
    ObjectRepository<P> objectRepository) {
    this.objectFactory = objectFactory.with(new SoftCopyBinaryRepository<>());
    this.objectRepository = objectRepository;
  }

  @Override
  public CompletableFuture<Object<P>> execute(
    Object<P> sourceObject,
    Identifier targetIdentifier) {
    final Object<P> targetObject = objectFactory.create(targetIdentifier);

    markSoftCopied(sourceObject, targetObject);
    increaseReferencedCount(sourceObject);

    // TODO: atomic update
    // TODO: transaction
    return pretendingToCopy(sourceObject, targetObject)
      .thenCompose(copiedObject -> CompletableFuture
        .allOf(
          objectRepository.save(sourceObject),
          objectRepository.save(targetObject))
        .thenApply(__ -> copiedObject));
  }

  private void markSoftCopied(Object<P> sourceObject, Object<P> targetObject) {
    final Identifier sourceIdentifier = sourceObject.getIdentifier();
    targetObject.addTag(TAG_COPYING_SOFT_COPIED);
    targetObject.addTag(TAG_COPYING_SOFT_COPIED_SOURCE_AREA, sourceIdentifier.getArea());
    targetObject.addTag(TAG_COPYING_SOFT_COPIED_SOURCE_KEY, sourceIdentifier.getKey());
  }

  @SuppressWarnings("unchecked")
  private CompletableFuture<Object<P>> pretendingToCopy(Object<P> sourceObject,
    Object<P> targetObject) {
    final Payload notifyPayload = () -> sourceObject.getSize();
    return targetObject.copyFrom(adapt(Mono.just((P) notifyPayload)));
  }

  private void increaseReferencedCount(Object<P> sourceObject) {
    final long softCount = sourceObject
      .getTag(TAG_COPYING_SOFT_COPIED_SOURCE_REFERENCED_COUNT)
      .asLongOrDefault(0L);
    sourceObject.addTag(TAG_COPYING_SOFT_COPIED_SOURCE_REFERENCED_COUNT, softCount + 1);
  }

  public static class SoftCopyBinaryRepository<P extends Payload> implements BinaryRepository<P> {
    @Override
    public CompletableFuture<BinaryPublisher<P>> find(BinaryPath binaryPath) {
      throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<Void> append(BinaryPath binaryPath,
      BinaryPublisher<P> binaryPublisher) {
      throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<Void> delete(BinaryPath binaryPath) {
      throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<Void> create(BinaryPath binaryPath,
      BinaryPublisher<P> binaryPublisher) {
      final CompletableFuture<Void> ret = new CompletableFuture<>();
      binaryPublisher.subscribe(new NoOperationSubscriber<>() {
        @Override
        public void onSubscribe(Subscription s) {
          s.request(1L);
        }

        @Override
        public void onComplete() {
          ret.complete(null);
        }

        @Override
        public void onError(Throwable t) {
          ret.completeExceptionally(t);
        }
      });
      return ret;
    }
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
