package io.lette1394.mediaserver.common;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class AggregateRoot {
  private final List<Event> events = new LinkedList<>();
  private final List<Event.Listener<Event>> listeners = new LinkedList<>();

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

  // TODO: 시간 복잡도! linked list
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
