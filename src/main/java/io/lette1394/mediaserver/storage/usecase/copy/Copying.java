package io.lette1394.mediaserver.storage.usecase.copy;

import static io.lette1394.mediaserver.storage.usecase.copy.SoftCopying.TAG_COPYING_SOFT_COPIED_SOURCE_REFERENCED_COUNT;
import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;

import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectRepository;
import io.lette1394.mediaserver.storage.domain.Payload;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;


// TODO: unit test
@Builder
@RequiredArgsConstructor
public class Copying<P extends Payload> {
  private final ObjectRepository<P> objectRepository;

  private final CopyStrategy<P> hardCopying;
  private final CopyStrategy<P> softCopying;
  private final CopyStrategy<P> replicatingHardCopying;

  public CompletableFuture<Object<P>> copy(Command command) {
    return objectRepository
      .find(command.from)
      .thenCompose(sourceObject -> Match(command.mode)
        .of(
          Case($(CopyMode.HARD), () -> hardCopying),
          Case($(CopyMode.SOFT), () -> {
            // TODO: refactoring
            if (needReplicating(command, sourceObject)) {
              return replicatingHardCopying;
            }
            return softCopying;
          }))
        .execute(sourceObject, command.to));
  }

  private boolean needReplicating(Command command, Object<P> sourceObject) {
    return sourceObject
      .getTag(TAG_COPYING_SOFT_COPIED_SOURCE_REFERENCED_COUNT)
      .asLongOrDefault(0L) >= command.replicatingThreshold;
  }

  public enum CopyMode {
    HARD, SOFT
  }

  @Value
  @Builder
  public static class Command {
    Identifier from;
    Identifier to;
    CopyMode mode;

    // TODO: fix it. HARD_COPY는 이 정보가 필요하지 않다.
    //  그냥 optional 로 주고 땡인가?
    long replicatingThreshold;
  }
}
