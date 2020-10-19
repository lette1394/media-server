package io.lette1394.mediaserver.domain.storage.infrastructure.filesystem;

import io.lette1394.mediaserver.domain.storage.object.BinaryRepository;
import io.lette1394.mediaserver.domain.storage.object.BinarySupplier;
import io.lette1394.mediaserver.domain.storage.object.Object;
import java.util.concurrent.CompletableFuture;

public class FileSystemBinaryRepository implements BinaryRepository {
  @Override
  public CompletableFuture<BinarySupplier> findBinary(Object object) {
    return null;
  }

  @Override
  public CompletableFuture<Void> createBinary(Object object, BinarySupplier binarySupplier) {
    return null;
  }

  @Override
  public CompletableFuture<Void> appendBinary(Object object, BinarySupplier binarySupplier) {
    return null;
  }
}
