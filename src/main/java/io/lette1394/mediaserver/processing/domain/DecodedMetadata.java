package io.lette1394.mediaserver.processing.domain;

import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DecodedMetadata {
//  public static final

  private final Map<String, String> holder;

  public String getAsString(String key) {
    return holder.get(key);
  }

  public long getAsLong(String key) {
    // TODO: fix naive implements
    return Long.parseLong(holder.get(key).replaceAll("\\D+",""));
  }

  public int getAsInt(String key) {
    return Integer.parseInt(holder.get(key));
  }

  @Override
  public String toString() {
    return holder.toString();
  }

  public Set<String> names() {
    return holder.keySet();
  }
}