package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.Event;
import lombok.RequiredArgsConstructor;

public interface ObjectEvents {

  @RequiredArgsConstructor
  class UploadingTriggered extends Event {
    public final Object object;
    public final BinaryRepository binaryRepository;

    public static Event uploadingTriggered(Object object, BinaryRepository binaryRepository) {
      return new UploadingTriggered(object, binaryRepository);
    }
  }

  @RequiredArgsConstructor
  class Uploaded extends Event {
    public final Object object;
    public final BinaryRepository binaryRepository;

    public static Event uploaded(Object object, BinaryRepository binaryRepository) {
      return new Uploaded(object, binaryRepository);
    }
  }

  @RequiredArgsConstructor
  class UploadAborted extends Event {
    public final Object object;
    public final Throwable throwable;

    public static Event uploadAborted(Object object, Throwable throwable) {
      return new UploadAborted(object, throwable);
    }
  }

  @RequiredArgsConstructor
  class DownloadingTriggered extends Event {
    public final Object object;

    public static Event downloadingTriggered(Object object) {
      return new DownloadingTriggered(object);
    }
  }

  @RequiredArgsConstructor
  class DownloadAborted extends Event {
    public final Object object;
    public final Throwable throwable;

    public static Event downloadAborted(Object object, Throwable throwable) {
      return new DownloadAborted(object, throwable);
    }
  }
}
