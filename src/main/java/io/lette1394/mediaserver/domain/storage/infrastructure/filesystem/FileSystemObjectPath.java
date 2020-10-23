package io.lette1394.mediaserver.domain.storage.infrastructure.filesystem;

import io.lette1394.mediaserver.domain.storage.infrastructure.ObjectPath;
import io.lette1394.mediaserver.domain.storage.object.Object;
import lombok.Value;

@Value
public class FileSystemObjectPath implements ObjectPath {
  Object object;

  @Override
  public String asString() {


    return null;
  }
}
