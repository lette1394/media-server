package io.lette1394.mediaserver.storage.domain;

import org.reactivestreams.Publisher;


public class PretendingBinarySupplier<B extends Payload> extends DelegatingBinarySupplier<B> {

  public PretendingBinarySupplier() {
    super(() -> subscriber -> {});
  }


}
