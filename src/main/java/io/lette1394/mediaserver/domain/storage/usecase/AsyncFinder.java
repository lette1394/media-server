package io.lette1394.mediaserver.domain.storage.usecase;

import io.lette1394.mediaserver.domain.storage.Identifier;
import io.lette1394.mediaserver.domain.storage.Object;
import java.util.concurrent.CompletableFuture;

public interface AsyncFinder extends Finder {

    CompletableFuture<Object> findAsync(Identifier identifier);

    @Override
    default Object find(Identifier identifier) throws ObjectNotFoundException {
        return findAsync(identifier).join();
    }
}
