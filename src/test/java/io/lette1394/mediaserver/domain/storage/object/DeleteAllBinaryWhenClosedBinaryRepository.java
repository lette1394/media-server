package io.lette1394.mediaserver.domain.storage.object;

import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.Value;

@Value
public class DeleteAllBinaryWhenClosedBinaryRepository implements AutoClosableBinaryRepository {
  Set<Object> createdObjects = new HashSet<>();
  BinaryRepository repository;

  @Override
  public CompletableFuture<BinarySupplier> findBinary(Object object) {
    return repository.findBinary(object);
  }

  @Override
  public CompletableFuture<Void> createBinary(Object object, BinarySupplier binarySupplier) {
    return repository.createBinary(object, binarySupplier)
      .thenAccept(__ -> memory(object));
  }

  @Override
  public CompletableFuture<Void> appendBinary(Object object, BinarySupplier binarySupplier) {
    return repository.appendBinary(object, binarySupplier);
  }

  @Override
  public CompletableFuture<Void> deleteBinary(Object object) {
    return repository
      .deleteBinary(object)
      .thenAccept(__ -> createdObjects.remove(object));
  }

  @Override
  public void close() throws Exception {
    final Set<CompletableFuture<Void>> collect = createdObjects
      .parallelStream()
      .map(this::deleteBinary)
      .collect(Collectors.toSet());

    collect.forEach(CompletableFuture::join);
  }

  private void memory(Object object) {
    createdObjects.add(object);
  }
}
