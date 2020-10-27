package io.lette1394.mediaserver.storage.domain.binary;

import lombok.Builder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Binary<BUFFER_TYPE> {

  private final Policy policy;
  private final BinarySupplier binarySupplier;


  public void upload() {

  }
}
