package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.AggregateRoot;
import io.lette1394.mediaserver.common.Event;
import io.lette1394.mediaserver.common.Notifiable;
import io.lette1394.mediaserver.storage.domain.object.Policy;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Object extends AggregateRoot implements Notifiable {
  protected final Policy policy;


  public <T extends SizeAware> CompletableFuture<Object> upload(Binary<T> binary) {
//    binary.upload();

    return null;
  }

  @Override
  public void notify(Event event) {
    addEvent(event);
  }
}
