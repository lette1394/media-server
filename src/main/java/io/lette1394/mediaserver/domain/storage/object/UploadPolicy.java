package io.lette1394.mediaserver.domain.storage.object;

import static java.util.concurrent.CompletableFuture.completedFuture;

import io.lette1394.mediaserver.common.Result;

public interface UploadPolicy {


  @FunctionalInterface
  interface ObjectUploadPolicy extends BiTestable<Object, BinaryRepository> {
  }
}
