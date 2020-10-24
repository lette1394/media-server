package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.Event.ContextBoundedEvent;
import lombok.RequiredArgsConstructor;

public interface ObjectEvents {

  @RequiredArgsConstructor(staticName = "uploadingTriggered")
  class UploadingTriggered extends ContextBoundedEvent {
    public final Object object;
    public final BinaryRepository binaryRepository;
  }

  @RequiredArgsConstructor(staticName = "uploaded")
  class Uploaded extends ContextBoundedEvent {
    public final Object object;
    public final BinaryRepository binaryRepository;
  }

  @RequiredArgsConstructor(staticName = "uploadAborted")
  class UploadAborted extends ContextBoundedEvent {
    public final Object object;
    public final Throwable throwable;
  }

  @RequiredArgsConstructor(staticName = "downloadingTriggered")
  class DownloadingTriggered extends ContextBoundedEvent {
    public final Object object;
  }

  @RequiredArgsConstructor(staticName = "downloadAborted")
  class DownloadAborted extends ContextBoundedEvent {
    public final Object object;
    public final Throwable throwable;
  }
}
