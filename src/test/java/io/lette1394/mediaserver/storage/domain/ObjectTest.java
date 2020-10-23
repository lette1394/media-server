package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.storage.InMemoryStorage;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

class ObjectTest {
  private final static String AREA_NAME = "TEST-AREA";
  private final static String OBJECT_KEY = "OBJECT_KEY_0001";

  private final static byte[] testBinary = RandomStringUtils.random(1000).getBytes();

  @Test
  void upload() {
    final ObjectFactory factory = new ObjectFactory(new InMemoryStorage());
    final Object object = factory.create(AREA_NAME, OBJECT_KEY);

  }

}