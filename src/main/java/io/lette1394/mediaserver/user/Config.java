package io.lette1394.mediaserver.user;

import lombok.Value;

@Value
public class Config {
  CopyMode copyMode;
  Long replicatingSoftThreshold;

  public enum CopyMode {
    HARD,
    SOFT
  }
}
