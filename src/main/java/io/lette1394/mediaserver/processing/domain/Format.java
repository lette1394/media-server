package io.lette1394.mediaserver.processing.domain;

public enum Format {
  JPEG,
  PNG,
  GIF,
  TIFF,
  BMP,

  MP4,
  MKV,
  FLV,
  MOV,
  AVI;

  public boolean isImage() {
    return false;
  }
  public boolean isVideo() {
    return false;
  }
  public boolean isAudio() {
    return false;
  }
  public boolean isFile() {
    return false;
  }
}
