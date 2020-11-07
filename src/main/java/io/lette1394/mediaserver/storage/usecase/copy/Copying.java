package io.lette1394.mediaserver.storage.usecase.copy;

import io.lette1394.mediaserver.storage.domain.BinaryPath;
import io.lette1394.mediaserver.storage.domain.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectFactory;
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

//  private final CopyStrategy<BUFFER> copyStrategy;

  // 1. 무조건 link
  // 2. source에 몇 개가 link 되어있냐에 따라서 조건별 link
  // 3. source에 몇 개가 link 되어있냐에 따라서 조건별 replica
  //
  // hard copy / replicating hard copy / soft copy

  public CompletableFuture<Object<BUFFER>> copy(Command command) {
    return objectRepository
      .find(command.from)
//      .thenCompose(sourceObject -> copyStrategy.execute())
      .thenCompose(sourceObject -> overwriteAlways(sourceObject, command.to));
  }

  private CompletableFuture<Object<BUFFER>> hardCopy(
    Object<BUFFER> sourceObject, Identifier to) {

    final Object<BUFFER> targetObject = objectFactory.create(to);
    return sourceObject.download()
      .thenApply(sourceBinary -> targetObject.copyFrom(sourceBinary))
      .thenCompose(sourceBinary -> binaryRepository.create(BinaryPath.from(to), sourceBinary))
      // TODO: handle? 예외 생겼을 때 롤백?
      .thenCompose(__ -> objectRepository.save(targetObject));
  }

  private CompletableFuture<Object<BUFFER>> softCopy(
    Object<BUFFER> sourceObject,
    Identifier to) {

    // 기존 객체 바라봄
    // 기존 객체 link count +1
    final long CRITICAL_POINT = 2;
    final long softCount = sourceObject.getTag("copying.soft.count").asLong();

    sourceObject.addTag("copying.soft.copied.count", softCount + 1);

    final Object<BUFFER> targetObject = objectFactory.create(to);
    final Identifier identifier = targetObject.getIdentifier();
    targetObject.addTag("copying.soft.copied");
    targetObject.addTag("copying.soft.copied.source.area", identifier.getArea());
    targetObject.addTag("copying.soft.copied.source.key", identifier.getKey());

    return objectRepository.save(targetObject);
  }


  private CompletableFuture<Object<BUFFER>> overwriteAlways(
    Object<BUFFER> sourceObject,
    Identifier to) {

    final boolean always_use_hard_copy = true;
    final long CRITICAL_POINT = 2;
    final long softCount = sourceObject.getTag("copying.soft.count").asLong();

    // hard copy
    if (always_use_hard_copy) {
      return hardCopy(sourceObject, to);
    } else {

      // soft copy
      if (softCount < CRITICAL_POINT) {
        return softCopy(sourceObject, to);
      }

      // replicating hard copy
      // 똑같은 새로운 객체를 만들고
      // 다음 link 부터는 이 객체를 가지고 copy함
      return hardCopy(sourceObject, to)
        .thenCompose(copiedObject -> {
          final Identifier identifier = copiedObject.getIdentifier();

          sourceObject.addTag("copying.replica.target.area", identifier.getArea());
          sourceObject.addTag("copying.replica.target.key", identifier.getKey());

          return objectRepository
            .save(sourceObject)
            .thenApply(__ -> copiedObject);
        });
    }
  }

  @Value
  @Builder
  public static class Command {
    Identifier from;
    Identifier to;
  }
}
