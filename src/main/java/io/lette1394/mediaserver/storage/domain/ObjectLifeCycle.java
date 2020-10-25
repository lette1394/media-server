package io.lette1394.mediaserver.storage.domain;

public enum ObjectLifeCycle {
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

  public boolean is(ObjectLifeCycle objectLifeCycle) {
    return this == objectLifeCycle;
  }

  public boolean isCompletedExceptionally() {
    return false;
  }

  public boolean isCompletedNormally() {
    return false;
  }

  public boolean isCompleted() {
    return false;
  }
}
