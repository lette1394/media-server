package io.lette1394.mediaserver.processing.usecase;

import io.lette1394.mediaserver.storage.domain.BinarySupplier;
import java.util.Map;
import lombok.Value;

public class UploadingMedia {
  @Value
  public static class Command {
    String area;
    String key;
    Map<String, String> tags;
    BinarySupplier binarySupplier;
  }
}
