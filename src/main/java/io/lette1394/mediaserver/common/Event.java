package io.lette1394.mediaserver.common;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.EventListener;
import lombok.SneakyThrows;

public interface Event {

  interface FailureEvent extends Event {
    Throwable getThrowable();
  }

  @FunctionalInterface
  interface Listener<T extends Event> extends EventListener {

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

  interface Publisher<T extends Event> {

    void publish(Event event);
  }
}
