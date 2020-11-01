package io.lette1394.mediaserver.storage.domain;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.Value;

@Value
public class DeleteAllBinaryWhenClosedBinaryRepository<T extends Payload> implements
  AutoClosableBinaryRepository<T> {

  Set<Identifier> createdObjects = new HashSet<>();
  BinaryRepository<T> repository;

  @Override
  public CompletableFuture<BinarySupplier<T>> find(BinaryPath binaryPath) {
    return repository.find(binaryPath);
  }

  @Override
  public CompletableFuture<Void> append(BinaryPath binaryPath, BinarySupplier<T> binarySupplier) {
    return repository.append(binaryPath, binarySupplier);
  }

  @Override
  public CompletableFuture<Void> delete(BinaryPath binaryPath) {
    return repository.delete(binaryPath);
  }

  @Override
  public CompletableFuture<Void> create(BinaryPath binaryPath, BinarySupplier<T> binarySupplier) {
    return repository.create(binaryPath, binarySupplier);
  }

  @Override
  public void close() throws Exception {
    final Set<CompletableFuture<Void>> collect = createdObjects
      .parallelStream()
      .map((Identifier identifier) -> delete(BinaryPath.from(identifier)))
      .collect(Collectors.toSet());

    collect.forEach(CompletableFuture::join);
  }
}
