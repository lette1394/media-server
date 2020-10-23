package io.lette1394.mediaserver.processing.usecase;

import io.lette1394.mediaserver.common.Result;
import io.lette1394.mediaserver.processing.domain.MediaObject;
import io.lette1394.mediaserver.storage.domain.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.Storage;
import io.lette1394.mediaserver.storage.usecase.Uploading;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.Value;

public class UploadingMedia {
  Storage storage;


  public CompletableFuture<Result<Void>> upload(Command command) {
    final Uploading uploading = new Uploading(storage);
    new MediaObject();




    return null;
  }

  @Value
  public static class Command {
    String area;
    String key;
    Map<String, String> tags;
    BinarySupplier binarySupplier;
  }
}
