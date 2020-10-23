package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.Result;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.Value;

@Value
public class DeleteAllBinaryWhenClosedBinaryRepository implements AutoClosableBinaryRepository {
  Set<Identifier> createdObjects = new HashSet<>();
  BinaryRepository repository;

  @Override
  public CompletableFuture<Result<BinarySupplier>> findBinary(
    Identifier identifier) {
    return repository.findBinary(identifier);
  }

  @Override
  public CompletableFuture<Result<Void>> createBinary(Identifier identifier,
    BinarySupplier binarySupplier) {
    return repository.createBinary(identifier, binarySupplier)
      .thenAccept(__ -> memory(identifier))
      .thenApply(aVoid -> Result.succeed());
  }

  @Override
  public CompletableFuture<Result<Void>> appendBinary(Identifier identifier,
    BinarySupplier binarySupplier) {
    return repository.appendBinary(identifier, binarySupplier);
  }

  @Override
  public CompletableFuture<Result<Void>> deleteBinary(Identifier identifier) {
    return repository
      .deleteBinary(identifier)
      .thenAccept(__ -> createdObjects.remove(identifier))
      .thenApply(aVoid -> Result.succeed());
  }

  @Override
  public void close() throws Exception {
    final Set<CompletableFuture<Result<Void>>> collect = createdObjects
      .parallelStream()
      .map(this::deleteBinary)
      .collect(Collectors.toSet());

    collect.forEach(CompletableFuture::join);
  }

  private void memory(Identifier identifier) {
    createdObjects.add(identifier);
  }
}
