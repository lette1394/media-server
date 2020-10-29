package io.lette1394.mediaserver.storage.infrastructure.filesystem;

import io.lette1394.mediaserver.storage.infrastructure.ObjectPath;
import io.lette1394.mediaserver.storage.domain.Object;
import lombok.Value;

@Value
public class FileSystemObjectPath implements ObjectPath {
  Object object;

  @Override
  public String asString() {


    return null;
  }
}
