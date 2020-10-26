package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.storage.domain.binary.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.binary.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.object.Identifier;
import io.lette1394.mediaserver.storage.domain.object.Object;
import io.lette1394.mediaserver.storage.domain.object.ObjectRepository;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;

public interface Storage<T extends BinarySupplier> extends ObjectRepository, BinaryRepository<T> {

  class StorageBuilder<T extends BinarySupplier> {
    ObjectRepository objects;
    BinaryRepository<T> binaries;

    @Builder
    public StorageBuilder(
      ObjectRepository objects,
      BinaryRepository<T> binaries) {
      this.objects = objects;
      this.binaries = binaries;
    }

    public Storage<T> toStorage() {
      return new Storage<>() {
        @Override
        public CompletableFuture<T> findBinary(Identifier identifier) {
          return binaries.findBinary(identifier);
        }

        @Override
        public CompletableFuture<Void> saveBinary(Identifier identifier, T binarySupplier) {
          return binaries.saveBinary(identifier, binarySupplier);
        }

        @Override
        public CompletableFuture<Void> appendBinary(Identifier identifier, T binarySupplier) {
          return binaries.appendBinary(identifier, binarySupplier);
        }

        @Override
        public CompletableFuture<Void> deleteBinary(Identifier identifier) {
          return binaries.deleteBinary(identifier);
        }

        @Override
        public CompletableFuture<Boolean> objectExists(Identifier identifier) {
          return objects.objectExists(identifier);
        }

        @Override
        public CompletableFuture<Object> findObject(Identifier identifier) {
          return objects.findObject(identifier);
        }

        @Override
        public CompletableFuture<Object> saveObject(Object object) {
          return objects.saveObject(object);
        }

        @Override
        public CompletableFuture<Void> deleteObject(Identifier identifier) {
          return objects.deleteObject(identifier);
        }
      };
    }
  }
}
