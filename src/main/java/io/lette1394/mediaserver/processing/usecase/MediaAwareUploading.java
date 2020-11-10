package io.lette1394.mediaserver.processing.usecase;

import io.lette1394.mediaserver.processing.domain.MediaDecoder;
import io.lette1394.mediaserver.storage.domain.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.Payload;
import io.lette1394.mediaserver.storage.usecase.Uploading;
import io.lette1394.mediaserver.storage.usecase.Uploading.Command;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class MediaAwareUploading<B extends Payload> {
  private final Uploading<B> uploading;
  private final MediaDecoder mediaDecoder;


  // 방법 1. MediaAwareBinarySupplier를 도입한다
  // 방법 2. MediaDecoder 같은걸로 명시적으로 decode...

  // 방법 1이 더 나아보이니까 일단 이렇게하자

  public CompletableFuture<Void> upload(Command<B> command) {

    final CompletableFuture<Object<B>> upload = uploading.upload(command);

    return CompletableFuture.completedFuture(null);
  }
}
