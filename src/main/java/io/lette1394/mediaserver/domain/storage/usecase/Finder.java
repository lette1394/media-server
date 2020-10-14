package io.lette1394.mediaserver.domain.storage.usecase;

import io.lette1394.mediaserver.domain.storage.object.Identifier;
import io.lette1394.mediaserver.domain.storage.object.Object;

@FunctionalInterface
interface Finder {
  StorageResult<Object> find(Identifier identifier) throws ObjectNotFoundException;
}
