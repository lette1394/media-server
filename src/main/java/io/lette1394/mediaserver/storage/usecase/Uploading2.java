package io.lette1394.mediaserver.storage.usecase;

import io.lette1394.mediaserver.common.Event;
import io.lette1394.mediaserver.common.Event.Publisher;
import io.lette1394.mediaserver.storage.domain.Binary;
import io.lette1394.mediaserver.storage.domain.Object;

public class Uploading2 {
  Publisher<Event> publisher;

  public void upload() {
    Object object = new Object(null);
    object.upload(null)
      .thenAccept(object2 -> publisher.publish(object2.getEvents()));
  }

  public void upload2() {

  }

}
