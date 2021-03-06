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
    // TODO: 외부에서 아예 생성해서 주자.... 인데
    //  어짜피 SoftCopyBinaryRepository 이것만 써야하는데 이 경우에서도 밖에서 주입해주는 형태가 맞나?
    //  그러면 불변식이 깨질 수가 있는데...
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
    // TODO: handle failure
    return pretendingUpload(sourceObject, targetObject)
      .thenCompose(copiedObject -> CompletableFuture
        .allOf(
          objectRepository.save(sourceObject),
          objectRepository.save(targetObject))
        .thenApply(__ -> copiedObject));

    // COPY INVARIANTS: source/target 은 서로 atomic하게 업데이트 되어야한다
    //  copy는 그 자체로 usecase 이므로 이는 domain model이 아닌 usecase에 있는다...?
    //  .
    //  .
    //  .
    //  아닌 거 같다. usecase가 애초에 무엇이지?
    //  usecase는 domain 객체끼리의 흐름과 조합을 나타내는 건데...
    //  음...
    //  그러면 맞는 말 같은데?
    //  upload() / download() 간의 흐름과 조합을 나타내고 있잖아 지금
    //  어렵다 어려워
    //  .
    //  .
    //  .
    //  아... 이게 sub-domain 이구나....!!!!!
    //  와우.
    //  와우.
    //  와우.
  }

  private void markSoftCopied(Object<P> sourceObject, Object<P> targetObject) {
    final Identifier sourceIdentifier = sourceObject.getIdentifier();
    targetObject.addTag(TAG_COPYING_SOFT_COPIED);
    targetObject.addTag(TAG_COPYING_SOFT_COPIED_SOURCE_AREA, sourceIdentifier.getArea());
    targetObject.addTag(TAG_COPYING_SOFT_COPIED_SOURCE_KEY, sourceIdentifier.getKey());
  }

  @SuppressWarnings("unchecked")
  private CompletableFuture<Object<P>> pretendingUpload(Object<P> sourceObject,
    Object<P> targetObject) {
    final Payload notifyPayload = () -> sourceObject.getSize();
    return targetObject.upload(adapt(Mono.just((P) notifyPayload)));
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
  public static class SoftCopyFollowingObjectRepository<P extends Payload>
    implements ObjectRepository<P> {

    private final ObjectRepository<P> delegate;

    @Override
    public CompletableFuture<Boolean> exists(Identifier identifier) {
      return delegate.exists(identifier);
    }

    @Override
    public CompletableFuture<Object<P>> find(Identifier identifier)
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
    public CompletableFuture<Object<P>> save(Object<P> object) {
      return delegate.save(object);
    }

    @Override
    public CompletableFuture<Void> delete(Identifier identifier) {
      return delegate.delete(identifier);
    }
  }
}
