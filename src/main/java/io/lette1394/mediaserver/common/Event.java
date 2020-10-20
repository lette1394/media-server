package io.lette1394.mediaserver.common;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.EventListener;
import java.util.UUID;
import lombok.SneakyThrows;

public class Event {
  protected final Instant when = Instant.now();
  protected final UUID eventId = UUID.randomUUID();

  @FunctionalInterface
  public interface Listener<T extends Event> extends EventListener {
    @SneakyThrows
    default boolean canListen(T event) {
      Type mySuperclass = getClass().getGenericSuperclass();
      Type tType = ((ParameterizedType) mySuperclass).getActualTypeArguments()[0];
      String className = tType.toString().split(" ")[1];
      Class<?> inferredClass = Class.forName(className);

      return inferredClass.isAssignableFrom(event.getClass());
    }

    void handle(T event);
  }

  public interface Publisher<T extends Event> {
    void publish(Event event);
  }

  public static class ContextBoundedEvent extends Event {
  }
}
