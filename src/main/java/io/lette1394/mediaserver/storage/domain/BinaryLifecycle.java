package io.lette1394.mediaserver.storage.domain;

enum BinaryLifecycle {
  NO_OPERATION,
  BEFORE_TRANSFER,
  DURING_TRANSFERRING,
  AFTER_TRANSFERRED {
    @Override
    public boolean isCompletedNormally() {
      return true;
    }
  },
  TRANSFER_ABORTED {
    @Override
    public boolean isCompletedExceptionally() {
      return true;
    }
  };

  public boolean is(BinaryLifecycle binaryLifecycle) {
    return this == binaryLifecycle;
  }

  public boolean isCompletedExceptionally() {
    return false;
  }

  public boolean isCompletedNormally() {
    return false;
  }

  public boolean isCompleted() {
    return isCompletedNormally() || isCompletedExceptionally();
  }
}
