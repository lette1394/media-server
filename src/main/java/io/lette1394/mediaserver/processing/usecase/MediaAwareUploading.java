package io.lette1394.mediaserver.processing.usecase;

import io.lette1394.mediaserver.processing.domain.DecodedMetadata;
import io.lette1394.mediaserver.processing.domain.MediaDecoder;
import io.lette1394.mediaserver.processing.domain.PayloadParser;
import io.lette1394.mediaserver.processing.domain.PayloadParser.DataBufferPayloadParser;
import io.lette1394.mediaserver.processing.instrastructure.ApacheTikaMediaDecoder;
import io.lette1394.mediaserver.storage.domain.BinaryPublisher;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.Payload;
import io.lette1394.mediaserver.storage.domain.Tags;
import io.lette1394.mediaserver.storage.usecase.Uploading;
import io.lette1394.mediaserver.storage.usecase.Uploading.Command;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.model.Tag;

@RequiredArgsConstructor
public class MediaAwareUploading<B extends Payload> {
  private final Uploading<B> uploading;

  private final PayloadParser<B> payloadParser;
  // 방법 1. MediaAwareBinarySupplier를 도입한다
  // 방법 2. MediaDecoder 같은걸로 명시적으로 decode...

  // 방법 1이 더 나아보이니까 일단 이렇게하자

  public CompletableFuture<Object<B>> upload(Command<B> command) {
    final BinaryPublisher<B> broadcasted = command.getUpstream().broadcast(2);

    final ApacheTikaMediaDecoder<B> decoder = new ApacheTikaMediaDecoder<>(
      1024 * 1024,
      1024 * 1024 * 20,
      broadcasted,
      payloadParser);

    final CompletableFuture<Tags> tags = decoder.decode()
      .thenApply(metadata -> {
        final Tags empty = Tags.empty();
        metadata.names().forEach(name -> empty.addTag(name, metadata.getAsString(name)));
        return empty;
      });

    // TODO: Uploading class public method를 풍부하게 만들어야 할 거 같다...
    return uploading.upload(command.with(tags).with(broadcasted));
  }
}
