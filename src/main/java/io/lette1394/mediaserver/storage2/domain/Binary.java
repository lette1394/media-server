package io.lette1394.mediaserver.storage2.domain;

import static io.lette1394.mediaserver.storage2.domain.BinaryLifecycle.AFTER_TRANSFERRED;
import static io.lette1394.mediaserver.storage2.domain.BinaryLifecycle.BEFORE_TRANSFER;
import static io.lette1394.mediaserver.storage2.domain.BinaryLifecycle.DURING_TRANSFERRING;
import static io.lette1394.mediaserver.storage2.domain.BinaryLifecycle.TRANSFER_ABORTED;

import io.vavr.control.Try;
import lombok.Builder;

public class Binary<BUFFER extends SizeAware> {

  private final BinaryPolicy binaryPolicy;

  private final BinaryPath binaryPath;
  private final BinarySupplier<BUFFER> binarySupplier;
  private final BinarySnapshot snapshot = BinarySnapshot.initial();

  @Builder
  public Binary(BinaryPolicy binaryPolicy,
    BinaryPath binaryPath,
    BinarySupplier<BUFFER> binarySupplier) {
    this.binaryPolicy = binaryPolicy;
    this.binaryPath = binaryPath;
    this.binarySupplier = binarySupplier;
  }

  BinaryPath getBinaryPath() {
    return binaryPath;
  }

  BinarySupplier<BUFFER> getBinarySupplier() {
    return composeControllable(composeListenable(binarySupplier));
  }

  private BinarySupplier<BUFFER> composeControllable(BinarySupplier<BUFFER> binarySupplier) {
    return new ControllableBinarySupplier<>(binarySupplier, policy());
  }

  private BinarySupplier<BUFFER> composeListenable(BinarySupplier<BUFFER> binarySupplier) {
    return new ListenableBinarySupplier<>(binarySupplier, listener());
  }

  private ControllableBinarySupplier.Policy policy() {
    return new ControllableBinarySupplier.Policy() {
      @Override
      public Try<Void> beforeTransfer() {
        return binaryPolicy.test(snapshot.update(BEFORE_TRANSFER));
      }

      @Override
      public Try<Void> duringTransferring(long currentLength) {
        return binaryPolicy.test(
          snapshot
            .update(currentLength)
            .update(DURING_TRANSFERRING));
      }

      @Override
      public Try<Void> afterTransferred(long totalLength) {
        return binaryPolicy.test(
          snapshot
            .update(totalLength)
            .update(AFTER_TRANSFERRED));
      }
    };
  }

  private ListenableBinarySupplier.Listener listener() {
    return new ListenableBinarySupplier.Listener() {
      private boolean aborted = false;

      @Override
      public void beforeTransfer() {
        snapshot.update(BEFORE_TRANSFER);
      }

      @Override
      public void duringTransferring(long currentLength) {
        snapshot
          .update(currentLength)
          .update(DURING_TRANSFERRING);
      }

      @Override
      public void afterTransferred(long totalLength) {
        snapshot
          .update(totalLength)
          .update(AFTER_TRANSFERRED);
      }

      @Override
      public void transferAborted(Throwable throwable) {
        if (aborted) {
          return;
        }
        aborted = true;
        snapshot.update(TRANSFER_ABORTED);
      }
    };
  }
}
