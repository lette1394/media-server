package io.lette1394.mediaserver.storage.domain;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class BinaryFactory {

  Binary make(
    BinaryRepository binaryRepository,
    BinarySupplier<? extends SizeAware> binarySupplier) {
    return InitialBinary.builder()
      .binaryRepository(binaryRepository)
      .binarySupplier(binarySupplier)
      .build();
  }

  @Value
  public static class StringLike implements SizeAware {

    String value;

    @Override
    public long getSize() {
      return value.length();
    }
  }
}