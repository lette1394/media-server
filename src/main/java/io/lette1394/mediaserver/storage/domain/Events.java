package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.Event;
import io.lette1394.mediaserver.common.Event.FailureEvent;
import lombok.Value;

public interface Events {

  @Value(staticConstructor = "uploadRejected")
  class UploadRejected implements FailureEvent {
    Throwable throwable;
  }

  @Value(staticConstructor = "uploadAborted")
  class UploadAborted implements FailureEvent {
    Throwable throwable;
  }

  @Value(staticConstructor = "uploadingTriggered")
  class UploadingTriggered implements Event {
  }

  @Value(staticConstructor = "uploaded")
  class Uploaded implements Event {
  }

  @Value(staticConstructor = "downloadingTriggered")
  class DownloadingTriggered implements Event {

  }

  @Value(staticConstructor = "downloadRejected")
  class DownloadRejected implements FailureEvent {
    Throwable throwable;
  }

  @Value(staticConstructor = "downloaded")
  class Downloaded implements Event {
  }

  @Value(staticConstructor = "copyingTriggered")
  class CopyingTriggered implements Event {
  }

  @Value(staticConstructor = "copied")
  class Copied implements Event {
  }

  @Value(staticConstructor = "copyRejected")
  class CopyRejected implements FailureEvent {
    Throwable throwable;
  }
}
