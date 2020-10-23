package io.lette1394.mediaserver.storage.domain;

public class AccumulatingSizeBinarySupplier extends DelegatingBinarySupplier {
  public AccumulatingSizeBinarySupplier(BinarySupplier binarySupplier, Listener listener) {
    super(new ListenableBinarySupplier(binarySupplier, new ListenableBinarySupplier.Listener() {
      @Override
      public void duringTransferring(long currentSize, long total) {
        listener.currentSizeChanged(currentSize);
      }
    }));
  }

  @FunctionalInterface
  interface Listener {
    void currentSizeChanged(long currentSize);
  }
}
