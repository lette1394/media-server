package io.lette1394.mediaserver.domain.storage.object;

public enum ObjectLifeCycle {
  BEFORE_UPLOADING {
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

  BEFORE_DOWNLOADING {
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
