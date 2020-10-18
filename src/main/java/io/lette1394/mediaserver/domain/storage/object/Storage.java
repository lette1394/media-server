package io.lette1394.mediaserver.domain.storage.object;

import io.lette1394.mediaserver.common.AbstractionBoundary;
import io.lette1394.mediaserver.domain.storage.usecase.ObjectNotFoundException;
import io.lette1394.mediaserver.domain.storage.usecase.StorageResult;

@AbstractionBoundary
public interface Storage {
  StorageResult<Object> find(Identifier identifier) throws ObjectNotFoundException;

  StorageResult<BinarySupplier> findBinary(Object object);

  StorageResult<Void> create(Object object, BinarySupplier binarySupplier);

  StorageResult<Void> append(Object object, BinarySupplier binarySupplier);
}
