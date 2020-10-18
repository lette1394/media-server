package io.lette1394.mediaserver.domain.storage.object;

import io.lette1394.mediaserver.common.AbstractionBoundary;
import io.lette1394.mediaserver.domain.storage.usecase.ObjectNotFoundException;
import io.lette1394.mediaserver.domain.storage.usecase.StorageResult;

@AbstractionBoundary
public interface Storage {
  StorageResult<Object> find(Identifier identifier) throws ObjectNotFoundException;

  StorageResult<Void> upload(Object object, BinarySupplier binarySupplier);

  StorageResult<BinarySupplier> download(Object object);
}
