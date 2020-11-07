package io.lette1394.mediaserver.storage.usecase;

import io.lette1394.mediaserver.storage.domain.BinaryPath;
import io.lette1394.mediaserver.storage.domain.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectFactory;
import io.lette1394.mediaserver.storage.domain.ObjectNotFoundException;
import io.lette1394.mediaserver.storage.domain.ObjectRepository;
import io.lette1394.mediaserver.storage.domain.Payload;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class Copying<BUFFER extends Payload> {

  // TODO: [COPY] 그리고 여기서 object repository에 들어올 때, copy link 에 대해 알고 있는
  //  repository를 한 번 감싸서 object를 반환해주자.
  private final ObjectRepository<BUFFER> objectRepository;
  private final BinaryRepository<BUFFER> binaryRepository;
  private final ObjectFactory<BUFFER> objectFactory = new ObjectFactory<>();

  // 1. 무조건 link
  // 2. source에 몇 개가 link 되어있냐에 따라서 조건별 link
  // 3. source에 몇 개가 link 되어있냐에 따라서 조건별 replica
  //
  // hard copy / soft copy / replica

  public CompletableFuture<Object<BUFFER>> copy(Command command) {
    return objectRepository
      .find(command.from)
      .thenCompose(sourceObject -> overwriteAlways(sourceObject, command.to));
  }

  private CompletableFuture<Object<BUFFER>> overwriteAlways(Object<BUFFER> sourceObject,
    Identifier to) {

    final boolean always_use_hard_copy = true;
    final long CRITICAL_POINT = 2;
    final long softCount = sourceObject.getTag("copying.soft.count").asLong();

    if (always_use_hard_copy) {
      final Object<BUFFER> targetObject = objectFactory.create(to);
      return sourceObject.download()
        .thenApply(sourceBinary -> targetObject.copyFrom(sourceBinary))
        .thenCompose(sourceBinary -> {
          final BinaryPath targetBinaryPath = BinaryPath.from(to);
          return binaryRepository.create(targetBinaryPath, sourceBinary)
            .handle((__, e) -> objectRepository.save(targetObject))
            .thenCompose(__ -> __);
        });
    } else {

      if (softCount < CRITICAL_POINT) {
        // 기존 객체 바라봄
        // 기존 객체 link count +1
        sourceObject.addTag("copying.soft.copied.count", softCount + 1);

        final Object<BUFFER> targetObject = objectFactory.create(to);
        final Identifier identifier = targetObject.getIdentifier();
        targetObject.addTag("copying.soft.copied");
        targetObject.addTag("copying.soft.copied.source.area", identifier.getArea());
        targetObject.addTag("copying.soft.copied.source.key", identifier.getKey());

        // 내부 binary path를 어떻게 override 할까...?
        // source의 identifier만 저장하면 될 거 같은데 ...
        // 어떻게 저장하지? tag로 저장하면 될 거 같은데 이거 저장은 ok인데
        // 다시 instance화 할때는...?
        //

      }

      // 똑같은 새로운 객체를 만들고
      // 다음 link 부터는 이 객체를 가지고 copy함
    }

    return null;
  }

  @Value
  @Builder
  public static class Command {

    Identifier from;
    Identifier to;
  }

  @RequiredArgsConstructor
  private static class SoftCopyFollowingObjectRepository<BUFFER extends Payload> implements
    ObjectRepository<BUFFER> {

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
          if (maybeSoftCopiedObject.hasTag("copying.soft.copied")) {
            return toObject(maybeSoftCopiedObject);
          }
          return maybeSoftCopiedObject;
        });

      return null;
    }

    private Object<BUFFER> toObject(Object<BUFFER> object) {
      // TODO: object 에 binary path를 주입 가능하게 변경
      //  여기서 새로운 object로 치환
      //  구현체는 object factory 에 두고 사용하기
      return object;
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
