package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.Notifiable;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;

public class InitialBinary extends Binary {

  @Builder
  public InitialBinary(Notifiable root,
    BinaryPolicy policy,
    BinarySupplier<? extends SizeAware> binarySupplier,
    BinaryRepository binaryRepository) {
    super(root, policy, binarySupplier, binaryRepository);
  }

  @Override
  protected CompletableFuture<Void> doUpload(BinarySupplier<? extends SizeAware> binarySupplier) {
    return binaryRepository.save(null, binarySupplier);
  }
}
