package io.lette1394.mediaserver.domain.storage.usecase;

import io.lette1394.mediaserver.domain.storage.Object;

public interface AsyncDownloader {

    AsyncDataProvider downloadAsync(Object object);
}
