package io.lette1394.mediaserver.storage.infrastructure.awss3;

import io.lette1394.mediaserver.storage.infrastructure.ObjectPath;
import io.lette1394.mediaserver.storage.domain.Identifier;
import lombok.Value;

@Value
public class AwsObjectPath implements ObjectPath {
  Identifier identifier;

  @Override
  public String asString() {
    return null;
  }
}
