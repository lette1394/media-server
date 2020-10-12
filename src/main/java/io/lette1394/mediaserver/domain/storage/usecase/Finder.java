package io.lette1394.mediaserver.domain.storage.usecase;

import io.lette1394.mediaserver.domain.storage.Identifier;
import io.lette1394.mediaserver.domain.storage.Object;

interface Finder {
    Object find(Identifier identifier) throws ObjectNotFoundException;
}
