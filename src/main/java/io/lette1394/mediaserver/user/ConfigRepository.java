package io.lette1394.mediaserver.user;

import io.lette1394.mediaserver.user.Config.CopyMode;

public class ConfigRepository {

  public Config get(String serviceCode, String spaceId) {
    return new Config(CopyMode.HARD, 10);
  }
}
