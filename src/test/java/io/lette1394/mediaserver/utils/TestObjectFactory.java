package io.lette1394.mediaserver.utils;

import io.lette1394.mediaserver.domain.storage.infrastructure.filesystem.FileSystemBinaryRepository;
import io.lette1394.mediaserver.domain.storage.object.BinaryRepository;
import io.lette1394.mediaserver.domain.storage.object.DeleteAllBinaryWhenClosedBinaryRepository;
import io.lette1394.mediaserver.domain.storage.object.ObjectFactory;

public class TestObjectFactory extends ObjectFactory {

  public TestObjectFactory() {
    super(new DeleteAllBinaryWhenClosedBinaryRepository(
      new FileSystemBinaryRepository("out")));
  }


}
