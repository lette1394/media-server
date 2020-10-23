package io.lette1394.mediaserver.storage.domain;

public interface UploadPolicy {


  @FunctionalInterface
  interface ObjectUploadPolicy extends BiTestable<Object, BinaryRepository> {
  }
}
