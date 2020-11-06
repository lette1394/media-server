package io.lette1394.mediaserver.storage.usecase;

import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.Payload;
import java.util.concurrent.CompletableFuture;

public interface CopyStrategy<BUFFER extends Payload> {
  boolean matches(long softCopiedCount);

  CompletableFuture<Object<BUFFER>> execute(Object<BUFFER> source, Identifier target);
}
