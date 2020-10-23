package io.lette1394.mediaserver.storage.domain;

public enum ObjectLifeCycle {
  BEFORE_UPLOAD {
    @Override
    public boolean isBeforeUpload() {
      return true;
    }
  },

  DURING_UPLOADING {
    @Override
    public boolean isDuringUploading() {
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
    public boolean isBeforeDownload() {
      return true;
    }
  };

  public boolean isBeforeUpload() {
    return false;
  }

  public boolean isDuringUploading() {
    return false;
  }

  public boolean isAfterUploaded() {
    return false;
  }

  public boolean isBeforeDownload() {
    return false;
  }
}
