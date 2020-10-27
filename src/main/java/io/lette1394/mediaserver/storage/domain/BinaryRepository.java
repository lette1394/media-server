package io.lette1394.mediaserver.storage.domain;

import java.util.concurrent.CompletableFuture;

public interface BinaryRepository<BUFFER extends SizeAware> {

  CompletableFuture<? extends BinarySupplier<BUFFER>> find(BinaryPath binaryPath);

  CompletableFuture<Void> save(BinaryPath key, BinarySupplier<BUFFER> binarySupplier);

  CompletableFuture<Void> append(BinaryPath key, BinarySupplier<BUFFER> binarySupplier);

  CompletableFuture<Void> delete(BinaryPath key);
}
