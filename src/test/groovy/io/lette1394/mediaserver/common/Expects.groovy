package io.lette1394.mediaserver.common

import io.lette1394.mediaserver.storage.domain.Object
import io.lette1394.mediaserver.storage.domain.ObjectExpectDsl

import java.util.concurrent.CompletableFuture

class Expects {
  static <T> CompletableFutureExpectDsl<T> expect(CompletableFuture<T> future) {
    def dsl = new CompletableFutureExpectDsl<T>()
    dsl.future = future
    return dsl
  }

  static expect(Object object) {
    def dsl = new ObjectExpectDsl()
    dsl.object = object
    return dsl
  }
}
