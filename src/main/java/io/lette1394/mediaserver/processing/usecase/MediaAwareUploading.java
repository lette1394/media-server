package io.lette1394.mediaserver.processing.usecase;

import io.lette1394.mediaserver.processing.domain.MediaAwareBinaryPublisher;
import io.lette1394.mediaserver.storage.domain.BinaryPublisher;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.Payload;
import io.lette1394.mediaserver.storage.usecase.Uploading;
import io.lette1394.mediaserver.storage.usecase.Uploading.Command;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MediaAwareUploading<B extends Payload> {
  private final Uploading<B> uploading;
  private final Function<BinaryPublisher<B>, MediaAwareBinaryPublisher<B>> mediaAwareBinaryPublisher;

  // 방법 1. MediaAwareBinarySupplier를 도입한다
  // 방법 2. MediaDecoder 같은걸로 명시적으로 decode...

  // 방법 1이 더 나아보이니까 일단 이렇게하자

  public CompletableFuture<Object<B>> upload(Command<B> command) {
    final Command<B> newCommand = command.with(
      mediaAwareBinaryPublisher.apply(command.getUpstream()));
    return uploading.upload(newCommand);
  }
}
