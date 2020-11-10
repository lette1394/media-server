package io.lette1394.mediaserver.storage.domain;

import java.util.concurrent.CompletableFuture;

public interface BinaryRepository<BUFFER extends Payload> {

  default CompletableFuture<Boolean> exists(BinaryPath binaryPath) {
    return find(binaryPath)
      .thenApply(__ -> true)
      .exceptionally(__ -> false);
  }

  CompletableFuture<BinaryPublisher<BUFFER>> find(BinaryPath binaryPath);

  CompletableFuture<Void> append(BinaryPath binaryPath, BinaryPublisher<BUFFER> binaryPublisher);

  CompletableFuture<Void> delete(BinaryPath binaryPath);

  CompletableFuture<Void> create(BinaryPath binaryPath, BinaryPublisher<BUFFER> binaryPublisher);
}
