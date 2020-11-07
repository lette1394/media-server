package io.lette1394.mediaserver.storage.usecase;

import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.Payload;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;

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

      //
//      copyStrategies.stream()
//        .filter(strategy -> strategy.matches(softCopiedCount));
      return null;
    }
  }
}
