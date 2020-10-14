package io.lette1394.mediaserver.domain.storage.usecase;

import io.lette1394.mediaserver.domain.storage.object.Object;

@FunctionalInterface
interface Downloader {
  StorageResult<BinarySupplier> download(Object object);
}
