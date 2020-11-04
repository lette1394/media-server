package io.lette1394.mediaserver.storage.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.annotation.Nullable;

public class Context {
  private static final Context EMPTY = new Context();
  private final Map<java.lang.Object, java.lang.Object> holder = new HashMap<>();

  static Context empty() {
    return EMPTY;
  }

  @SuppressWarnings("unchecked")
  public <T> T get(java.lang.Object key) {
    return (T) holder.get(key);
  }

  public <T> T get(Class<T> key) {
    T v = get((java.lang.Object) key);
    if (key.isInstance(v)) {
      return v;
    }
    throw new NoSuchElementException("Context does not contain a value of type " + key
      .getName());
  }

  @Nullable
  public <T> T getOrDefault(java.lang.Object key, @Nullable T defaultValue) {
    if (!hasKey(key)) {
      return defaultValue;
    }
    return get(key);
  }

  public <T> Optional<T> getOrEmpty(java.lang.Object key) {
    if (hasKey(key)) {
      return Optional.of(get(key));
    }
    return Optional.empty();
  }

  public boolean hasKey(java.lang.Object key) {
    return holder.containsKey(key);
  }

  public boolean isEmpty() {
    return size() == 0;
  }

  public Context put(java.lang.Object key, java.lang.Object value) {
    final Context newContext = new Context();
    newContext.holder.putAll(holder);
    newContext.holder.put(key, value);
    return newContext;
  }

  public Context putAll(Context other) {
    final Context newContext = new Context();
    newContext.holder.putAll(holder);
    newContext.holder.putAll(other.holder);
    return newContext;
  }

  public Context putNonNull(java.lang.Object key, @Nullable java.lang.Object valueOrNull) {
    if (valueOrNull == null) {
      return this;
    }
    return put(key, valueOrNull);
  }

  public int size() {
    return holder.size();
  }
}
