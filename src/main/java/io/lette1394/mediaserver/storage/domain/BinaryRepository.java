package io.lette1394.mediaserver.storage.domain;

import java.util.concurrent.CompletableFuture;

public interface BinaryRepository<P extends Payload> {

  default CompletableFuture<Boolean> exists(BinaryPath binaryPath) {
    return find(binaryPath)
      .thenApply(__ -> true)
      .exceptionally(__ -> false);
  }

  CompletableFuture<BinaryPublisher<P>> find(BinaryPath binaryPath);

  CompletableFuture<Void> append(BinaryPath binaryPath, BinaryPublisher<P> binaryPublisher);

  CompletableFuture<Void> delete(BinaryPath binaryPath);

  CompletableFuture<Void> create(BinaryPath binaryPath, BinaryPublisher<P> binaryPublisher);
}
