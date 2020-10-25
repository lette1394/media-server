package io.lette1394.mediaserver.storage.domain.object;

public enum LifeCycle {
  NO_OPERATION,

  OPERATION_ABORTED {
    @Override
    public boolean isCompletedExceptionally() {
      return true;
    }
  },

  BEFORE_UPLOAD,

  DURING_UPLOADING,

  AFTER_UPLOADED {
    @Override
    public boolean isCompletedNormally() {
      return true;
    }
  },

  BEFORE_DOWNLOAD;

  public boolean is(LifeCycle lifeCycle) {
    return this == lifeCycle;
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
