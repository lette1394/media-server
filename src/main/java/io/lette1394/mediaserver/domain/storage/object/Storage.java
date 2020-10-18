package io.lette1394.mediaserver.domain.storage.object;

import io.lette1394.mediaserver.common.AbstractionBoundary;
import io.lette1394.mediaserver.domain.storage.usecase.ObjectNotFoundException;
import java.util.concurrent.CompletableFuture;

@AbstractionBoundary
public interface Storage {

  CompletableFuture<Boolean> isExist(Identifier identifier) throws ObjectNotFoundException;

  CompletableFuture<Object> find(Identifier identifier) throws ObjectNotFoundException;

  CompletableFuture<BinarySupplier> findBinary(Object object);

  CompletableFuture<Void> create(Object object, BinarySupplier binarySupplier);

  CompletableFuture<Void> append(Object object, BinarySupplier binarySupplier);
}
