package io.lette1394.mediaserver.storage.domain;


public class PretendingBinaryPublisher<B extends Payload> extends DelegatingBinaryPublisher<B> {

  public PretendingBinaryPublisher() {
    super(() -> subscriber -> {});
  }


}
