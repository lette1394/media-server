package io.lette1394.mediaserver.processing.domain;

import java.util.Map;
import lombok.Value;

@Value
public class Metadata {
  Map<String, String> holder;

  public String getAsString(String key) {
    return holder.get(key);
  }

  public long getAsLong(String key) {
    return Long.parseLong(holder.get(key));
  }

  public int getAsInt(String key) {
    return Integer.parseInt(holder.get(key));
  }
}
