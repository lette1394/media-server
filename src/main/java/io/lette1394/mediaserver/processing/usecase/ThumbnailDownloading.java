package io.lette1394.mediaserver.processing.usecase;

import io.lette1394.mediaserver.storage.domain.Identifier;
import lombok.Value;


// media aware object repository...?
@Value
public class ThumbnailDownloading {
  @Value
  public static class Command {
    Identifier identifier;
  }
}
