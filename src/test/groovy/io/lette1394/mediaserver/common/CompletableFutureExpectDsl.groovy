package io.lette1394.mediaserver.common

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.not
import static org.junit.jupiter.api.Assertions.fail

class CompletableFutureExpectDsl<T> {
  CompletableFuture<T> future

  void gotCause(Class<? extends Throwable> expected) {
    try {
      future.join()
      fail("unreachable")
    } catch (Exception e) {
      if (e instanceof CompletionException) {
        assertThat(e.getCause(), is(not(null)))
        assertThat(e.getCause().getClass(), is(expected))
      } else {
        fail("unreachable")
      }
    }
  }

  @Override
  String toString() {
    return "Expect CompletableFuture"
  }
}
