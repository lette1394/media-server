package io.lette1394.mediaserver.storage.usecase;

import io.lette1394.mediaserver.storage.domain.Storage;
import io.lette1394.mediaserver.storage.domain.binary.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.object.Events.UploadRejected;
import io.lette1394.mediaserver.storage.domain.object.Events.UploadingTriggered;
import io.lette1394.mediaserver.storage.domain.object.Factory;
import io.lette1394.mediaserver.storage.domain.object.Identifier;
import io.lette1394.mediaserver.storage.domain.object.Object;
import io.lette1394.mediaserver.storage.domain.object.Policy;
import io.vavr.control.Either;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;
import lombok.Value;

@Value
public class Uploading {
  Storage storage;

  public CompletableFuture<Object> upload(Command command) {
    final Factory factory = new Factory(Policy.ALL_POLICY);
    final Identifier identifier = command.identifier;
    final Object object = factory.create(identifier.getArea(), identifier.getKey());

    final Either<UploadRejected, UploadingTriggered> upload = object.upload();
    return null;
  }

  @Value
  @Builder
  public static class Command {
    Identifier identifier;
    Map<String, String> tags;
    BinarySupplier binarySupplier;
  }
}
