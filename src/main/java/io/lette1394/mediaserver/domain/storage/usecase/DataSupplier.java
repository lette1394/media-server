package io.lette1394.mediaserver.domain.storage.usecase;

import java.io.InputStream;

@FunctionalInterface
interface DataSupplier {
  InputStream get();
}
