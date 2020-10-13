package io.lette1394.mediaserver.domain.storage.usecase;

import io.lette1394.mediaserver.domain.storage.Object;

@FunctionalInterface
interface Downloader {
  DataSupplier download(Object object);
}
