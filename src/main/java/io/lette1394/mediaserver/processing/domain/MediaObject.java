package io.lette1394.mediaserver.processing.domain;

import io.lette1394.mediaserver.storage.domain.BinaryPublisher;
import io.lette1394.mediaserver.storage.domain.Payload;
import java.util.concurrent.CompletableFuture;
import lombok.Value;


// TODO: 1. Make Object interface
//  2. implements StorageObject
//  3. compose and implements MediaObject
//  4. add media related feature to MediaObject
//  5. add media policies
@Value
public class MediaObject<P extends Payload> {
  public CompletableFuture<Void> upload(BinaryPublisher<P> binaryPublisher) {


    return CompletableFuture.completedFuture(null);
  }

  public CompletableFuture<MediaObject<P>> convert() {
    return CompletableFuture.completedFuture(null);
  }
}
