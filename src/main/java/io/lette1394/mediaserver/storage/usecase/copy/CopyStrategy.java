package io.lette1394.mediaserver.storage.usecase.copy;

import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.Payload;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;


// TODO: 이거 음... 인터페이스가 사용하기에 좀 적절하지 않다.
//  단순히 execute만 있어야 할 거 같은데... 조건문은 어디에 넣지?
//  이거 matches() 가 아니라, pattern matching 으로 해야할 거 같다
public interface CopyStrategy<BUFFER extends Payload> {
  boolean matches(long softCopiedCount);

  CompletableFuture<Object<BUFFER>> execute(Object<BUFFER> sourceObject, Identifier targetIdentifier);

  @RequiredArgsConstructor
  class ApplyingFirstMatchedSequentially<BUFFER extends Payload> implements CopyStrategy<BUFFER> {
    private final List<CopyStrategy<BUFFER>> copyStrategies;

    @Override
    public boolean matches(long softCopiedCount) {
      return copyStrategies.stream()
        .anyMatch(strategy -> strategy.matches(softCopiedCount));
    }

    @Override
    public CompletableFuture<Object<BUFFER>> execute(
      Object<BUFFER> sourceObject,
      Identifier targetIdentifier) {

//      for (CopyStrategy<BUFFER> copyStrategy : copyStrategies) {
//        if (copyStrategy.matches())
//      }
//
//      copyStrategies.stream().filter()
//        .filter(strategy -> strategy.matches(softCopiedCount));
      return null;
    }
  }
}
