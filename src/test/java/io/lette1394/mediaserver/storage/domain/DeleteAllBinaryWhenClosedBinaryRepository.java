package io.lette1394.mediaserver.storage.domain;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.Value;

@Value
public class DeleteAllBinaryWhenClosedBinaryRepository implements
  AutoClosableBinaryRepository {

  Set<Identifier> createdObjects = new HashSet<>();
  BinaryRepository repository;

  @Override
  public CompletableFuture<? extends BinarySupplier> findBinary(Identifier identifier) {
    return repository.findBinary(identifier);
  }

  @Override
  public CompletableFuture<Void> saveBinary(Identifier identifier, BinarySupplier binarySupplier) {
    return repository.saveBinary(identifier, binarySupplier)
      .thenAccept(__ -> memory(identifier));
  }

  @Override
  public CompletableFuture<Void> appendBinary(Identifier identifier,
    BinarySupplier binarySupplier) {
    return repository.appendBinary(identifier, binarySupplier);
  }

  @Override
  public CompletableFuture<Void> deleteBinary(Identifier identifier) {
    return repository
      .deleteBinary(identifier)
      .thenAccept(__ -> createdObjects.remove(identifier));
  }

  @Override
  public void close() throws Exception {
    final Set<CompletableFuture<Void>> collect = createdObjects
      .parallelStream()
      .map(this::deleteBinary)
      .collect(Collectors.toSet());

    collect.forEach(CompletableFuture::join);
  }

  private void memory(Identifier identifier) {
    createdObjects.add(identifier);
  }
}
