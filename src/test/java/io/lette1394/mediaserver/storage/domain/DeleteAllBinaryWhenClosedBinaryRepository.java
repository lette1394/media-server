package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.storage.domain.binary.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.binary.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.object.Identifier;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.Value;

@Value
public class DeleteAllBinaryWhenClosedBinaryRepository<T extends BinarySupplier> implements
  AutoClosableBinaryRepository<T> {
  Set<Identifier> createdObjects = new HashSet<>();
  BinaryRepository<T> repository;

  @Override
  public CompletableFuture<? extends T> findBinary(Identifier identifier) {
    return repository.findBinary(identifier);
  }

  @Override
  public CompletableFuture<Void> saveBinary(Identifier identifier, T binarySupplier) {
    return repository.saveBinary(identifier, binarySupplier)
      .thenAccept(__ -> memory(identifier));
  }

  @Override
  public CompletableFuture<Void> appendBinary(Identifier identifier, T binarySupplier) {
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
