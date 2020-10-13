package io.lette1394.mediaserver.domain.storage.usecase;

import io.lette1394.mediaserver.common.Result;
import io.lette1394.mediaserver.domain.storage.Object;

@FunctionalInterface
interface Uploader {
  Result upload(Object object);
}
