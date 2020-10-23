package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.Event.ContextBoundedEvent;
import lombok.RequiredArgsConstructor;

public interface ObjectEvents {

  @RequiredArgsConstructor(staticName = "UploadingTriggered")
  class UploadingTriggered extends ContextBoundedEvent {
    public final Object object;
    public final BinaryRepository binaryRepository;
  }

  @RequiredArgsConstructor(staticName = "uploaded")
  class Uploaded extends ContextBoundedEvent {
    public final Object object;
    public final BinaryRepository binaryRepository;
  }

  @RequiredArgsConstructor(staticName = "downloadingTriggered")
  class DownloadingTriggered extends ContextBoundedEvent {
    public final Object object;
  }
}
