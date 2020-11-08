package io.lette1394.mediaserver.storage.usecase.copy;

import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
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

  private final CopyStrategy<BUFFER> copyStrategy;

  // 1. 무조건 link ??
  // 2. source에 몇 개가 link 되어있냐에 따라서 조건별 link
  // 3. source에 몇 개가 link 되어있냐에 따라서 조건별 replica
  //
  // hard copy / replicating hard copy / soft copy

  public CompletableFuture<Object<BUFFER>> copy(Command command) {
    return objectRepository
      .find(command.from)
      .thenCompose(sourceObject -> copyStrategy.execute(sourceObject, command.to));
  }

  @Value
  @Builder
  public static class Command {
    Identifier from;
    Identifier to;
  }

  public static class Config {
    boolean supportSoftCopy;
  }

  enum Type {
    SOFT, HARD
  }
}
