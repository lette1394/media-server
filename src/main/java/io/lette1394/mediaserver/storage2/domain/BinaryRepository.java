package io.lette1394.mediaserver.storage2.domain;

import java.util.concurrent.CompletableFuture;

public interface BinaryRepository<BUFFER extends SizeAware> {

  CompletableFuture<Boolean> exists(BinaryPath binaryPath);

  CompletableFuture<BinarySupplier<BUFFER>> find(BinaryPath binaryPath);

  CompletableFuture<Void> save(Binary<BUFFER> binary);

  CompletableFuture<Void> append(BinaryPath binaryPath, BinarySupplier<BUFFER> binarySupplier);

  CompletableFuture<Void> delete(BinaryPath binaryPath);
}
