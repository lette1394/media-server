package io.lette1394.mediaserver.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AggregateRoot {
  private final List<Event> events = new ArrayList<>();
  private final List<Event.Listener<Event>> listeners = new ArrayList<>();

  protected void addEvent(Event event) {
    events.add(event);
  }

  protected void addListener(Event.Listener<Event> listener) {
    listeners.add(listener);
  }

  protected void addListeners(List<Event.Listener<Event>> listeners) {
    this.listeners.addAll(listeners);
  }

  // TODO: 이거 필요한지 확인
  protected void addThenPublish(Event event) {
    addEvent(event);
    safePublish(event);
  }

  private void safePublish(Event event) {
    listeners.forEach(listener ->
      safeRun(() -> {
        if (listener.canListen(event)) {
          listener.handle(event);
        }
      }));
  }

  public List<Event> getEvents() {
    return Collections.unmodifiableList(events);
  }

  private void safeRun(Runnable runnable) {
    try {
      runnable.run();
    } catch (Exception ignored) {
    }
  }
}
