package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.storage.domain.binary.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.binary.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.object.Identifier;
import io.lette1394.mediaserver.storage.domain.object.Object;
import io.lette1394.mediaserver.storage.domain.object.ObjectRepository;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;

public interface Storage extends ObjectRepository, BinaryRepository {

  class StorageBuilder {
    ObjectRepository objects;
    BinaryRepository binaries;

    @Builder
    public StorageBuilder(
      ObjectRepository objects,
      BinaryRepository binaries) {
      this.objects = objects;
      this.binaries = binaries;
    }

    public Storage toStorage() {
      return new Storage() {
        @Override
        public CompletableFuture<BinarySupplier> findBinary(Identifier identifier) {
          return binaries.findBinary(identifier);
        }

        @Override
        public CompletableFuture<Void> saveBinary(Identifier identifier,
          BinarySupplier binarySupplier) {
          return binaries.saveBinary(identifier, binarySupplier);
        }

        @Override
        public CompletableFuture<Void> appendBinary(Identifier identifier,
          BinarySupplier binarySupplier) {
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
