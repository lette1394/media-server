package io.lette1394.mediaserver.processing.usecase;

import io.lette1394.mediaserver.storage.domain.Identifier;
import lombok.Value;

@Value
public class DownloadingMedia {
  @Value
  public static class Command {
    Identifier identifier;
  }
}
