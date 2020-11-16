package io.lette1394.mediaserver.processing.instrastructure;

import io.lette1394.mediaserver.processing.domain.DecodedMetadata;
import io.lette1394.mediaserver.processing.domain.MediaDecoder;
import io.lette1394.mediaserver.storage.domain.BinaryPublisher;
import io.lette1394.mediaserver.storage.domain.Payload;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AwsRekognitionMediaDecoder<P extends Payload> implements MediaDecoder<P> {
  


  @Override
  public CompletableFuture<DecodedMetadata> decode(BinaryPublisher<P> binaryPublisher) {
    return null;
  }
}
