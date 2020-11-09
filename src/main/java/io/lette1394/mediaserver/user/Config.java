package io.lette1394.mediaserver.user;

import lombok.Value;

@Value
public class Config {
  CopyMode copyMode;
  long replicatingSoftThreshold;

  public enum CopyMode {
    HARD,
    SOFT
  }
}
