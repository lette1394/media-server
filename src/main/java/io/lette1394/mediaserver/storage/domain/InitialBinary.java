package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.Notifiable;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;

public class InitialBinary<BUFFER extends SizeAware> extends Binary<BUFFER> {

  @Builder
  public InitialBinary(Notifiable root, BinaryPolicy policy,
    BinarySupplier<BUFFER> binarySupplier,
    BinaryRepository<BUFFER> binaryRepository) {
    super(root, policy, binarySupplier, binaryRepository);
  }

  @Override
  protected CompletableFuture<Void> doUpload(BinarySupplier<BUFFER> binarySupplier) {
    return binaryRepository.save(null, binarySupplier);
  }
}
