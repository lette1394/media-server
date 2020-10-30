package io.lette1394.mediaserver.processing.usecase;

import java.util.Map;
import lombok.Value;

public class UploadingMedia {
  @Value
  public static class Command {
    String area;
    String key;
    Map<String, String> tags;
  }
}
