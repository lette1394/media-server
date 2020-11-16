package io.lette1394.mediaserver.processing.usecase;

import io.lette1394.mediaserver.processing.domain.DecodedMetadata;
import io.lette1394.mediaserver.processing.domain.MediaDecoder;
import io.lette1394.mediaserver.storage.domain.BinaryPublisher;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.Payload;
import io.lette1394.mediaserver.storage.domain.Tags;
import io.lette1394.mediaserver.storage.usecase.Uploading;
import io.lette1394.mediaserver.storage.usecase.Uploading.Command;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MediaAwareUploading<P extends Payload> {
  private final Uploading<P> uploading;
  private final MediaDecoder<P> mediaDecoder;

  public CompletableFuture<Object<P>> upload(Command<P> command) {
    final BinaryPublisher<P> broadcast = command.getUpstream().broadcast(2);
    final CompletableFuture<Tags> tags = mediaDecoder
      .decode(broadcast)
      .thenApply(metadata -> toTags(metadata));

    return uploading.upload(
      command
        .with(tags)
        .with(broadcast));
  }

  private Tags toTags(DecodedMetadata metadata) {
    final Tags empty = Tags.empty();
    metadata.names().forEach(name -> empty.addTag("decoded-" + name, metadata.getAsString(name)));
    return empty;
  }
}
