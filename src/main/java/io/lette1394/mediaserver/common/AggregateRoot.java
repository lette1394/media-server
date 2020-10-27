package io.lette1394.mediaserver.common;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class AggregateRoot {

  private final List<Event> events = new LinkedList<>();

  protected void addEvent(Event event) {
    events.add(event);
  }

  public List<Event> getEvents() {
    return Collections.unmodifiableList(events);
  }
}
