package io.lette1394.mediaserver.domain.storage.usecase;

import io.lette1394.mediaserver.domain.storage.Object;

@FunctionalInterface
public interface AsyncDownloader {
  AsyncDataSupplier downloadAsync(Object object);
}
