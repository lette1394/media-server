package io.lette1394.mediaserver.storage.domain;

public enum ObjectLifeCycle {
  BEFORE_UPLOAD {
    @Override
    public boolean isBeforeUploading() {
      return true;
    }
  },

  AFTER_UPLOADED {
    @Override
    public boolean isAfterUploaded() {
      return true;
    }
  },

  BEFORE_DOWNLOAD {
    @Override
    public boolean isBeforeDownloading() {
      return true;
    }
  };

  public boolean isBeforeUploading() {
    return false;
  }

  public boolean isAfterUploaded() {
    return false;
  }

  public boolean isBeforeDownloading() {
    return false;
  }
}
