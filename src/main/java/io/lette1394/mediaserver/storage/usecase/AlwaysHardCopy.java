package io.lette1394.mediaserver.storage.usecase;

import io.lette1394.mediaserver.storage.domain.BinaryPath;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectFactory;
import io.lette1394.mediaserver.storage.domain.Payload;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AlwaysHardCopy<BUFFER extends Payload> implements CopyStrategy<BUFFER> {
  private final ObjectFactory<BUFFER> objectFactory;

  @Override
  public boolean matches(long softCopiedCount) {
    return true;
  }

  @Override
  public CompletableFuture<Object<BUFFER>> execute(Object<BUFFER> source, Identifier target) {
//    final Object<BUFFER> targetObject = objectFactory.create(target);
//    return source.download()
//      .thenApply(sourceBinary -> targetObject.copyFrom(sourceBinary))
//      .thenCompose(sourceBinary -> {
//        final BinaryPath targetBinaryPath = BinaryPath.from(target);
//        return binaryRepository.create(targetBinaryPath, sourceBinary)
//          .handle((__, e) -> objectRepository.save(targetObject))
//          .thenCompose(__ -> __);
//      });

    return null;
  }
}
