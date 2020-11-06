package io.lette1394.mediaserver.storage.usecase;

import io.lette1394.mediaserver.storage.domain.BinaryPath;
import io.lette1394.mediaserver.storage.domain.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectFactory;
import io.lette1394.mediaserver.storage.domain.ObjectRepository;
import io.lette1394.mediaserver.storage.domain.Payload;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class Copying<BUFFER extends Payload> {

  private final ObjectRepository<BUFFER> objectRepository;
  private final BinaryRepository<BUFFER> binaryRepository;
  private final ObjectFactory<BUFFER> objectFactory = new ObjectFactory<>();

  public CompletableFuture<Object<BUFFER>> copy(Command command) {
    return objectRepository
      .find(command.from)
      .thenCompose(sourceObject -> overwriteAlways(sourceObject, command.to));
  }

  private CompletableFuture<Object<BUFFER>> overwriteAlways(Object<BUFFER> sourceObject,
    Identifier to) {

    final Object<BUFFER> targetObject = objectFactory.create(to);
    return sourceObject.download()
      .thenApply(sourceBinary -> targetObject.copyFrom(sourceBinary))
      .thenCompose(sourceBinary -> {
        final BinaryPath targetBinaryPath = BinaryPath.from(to);
        return binaryRepository.create(targetBinaryPath, sourceBinary)
          .handle((__, e) -> objectRepository.save(targetObject))
          .thenCompose(__ -> __);
      });
  }

  @Value
  @Builder
  public static class Command {
    Identifier from;
    Identifier to;
  }
}
