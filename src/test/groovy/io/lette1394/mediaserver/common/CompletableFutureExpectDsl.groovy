package io.lette1394.mediaserver.common


import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException

class CompletableFutureExpectDsl {
  CompletableFuture<?> future

  static CompletableFutureExpectDsl expect(CompletableFuture<?> future) {
    def dsl = new CompletableFutureExpectDsl()
    dsl.future = future
    return dsl
  }

  void completedExceptionallyWith(Class<? extends Throwable> expected) {
    try {
      future.join()
    } catch (Exception e) {
      if (e instanceof CompletionException) {
        e.getCause() != null && e.getCause().getClass() == expected
      }
    }
  }
}
