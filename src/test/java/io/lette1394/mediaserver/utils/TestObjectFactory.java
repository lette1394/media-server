package io.lette1394.mediaserver.utils;

import io.lette1394.mediaserver.storage.infrastructure.filesystem.FileSystemBinaryRepository;
import io.lette1394.mediaserver.storage.domain.DeleteAllBinaryWhenClosedBinaryRepository;
import io.lette1394.mediaserver.storage.domain.object.ObjectFactory;

public class TestObjectFactory extends ObjectFactory {

  public TestObjectFactory() {
    super(new DeleteAllBinaryWhenClosedBinaryRepository(
      new FileSystemBinaryRepository("out")));
  }


}
