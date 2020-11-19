package io.lette1394.mediaserver.common;

import java.util.concurrent.CompletionException;

public class CompletableFutures {
  public static Throwable unwrap(Throwable throwable) {
    Throwable unwrapped = throwable;
    while (unwrapped instanceof CompletionException) {
      unwrapped = unwrapped.getCause();
    }
    return unwrapped == null ? throwable : unwrapped;
  }
}
