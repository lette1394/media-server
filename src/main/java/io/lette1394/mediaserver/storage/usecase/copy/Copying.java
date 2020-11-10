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

@Builder
@RequiredArgsConstructor
public class Copying<BUFFER extends Payload> {
  private final ObjectRepository<BUFFER> objectRepository;

  private final CopyStrategy<BUFFER> hardCopying;
  private final CopyStrategy<BUFFER> softCopying;
  private final CopyStrategy<BUFFER> replicatingHardCopying;

  public CompletableFuture<Object<BUFFER>> copy(Command command) {
    return objectRepository
      .find(command.from)
      .thenCompose(sourceObject -> Match(command.mode)
        .of(
          Case($(CopyMode.HARD), () -> hardCopying),
          Case($(CopyMode.SOFT), () -> {
            boolean exceedOrEqualsReplicatedLimit = sourceObject
              .getTag(TAG_COPYING_SOFT_COPIED_SOURCE_REFERENCED_COUNT)
              .asLongOrDefault(0L) >= command.replicatingThreshold;

            if (exceedOrEqualsReplicatedLimit) {
              return replicatingHardCopying;
            }
            return softCopying;
          }))
        .execute(sourceObject, command.to));
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
