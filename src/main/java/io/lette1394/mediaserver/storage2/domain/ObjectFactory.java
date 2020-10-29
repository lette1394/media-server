package io.lette1394.mediaserver.storage2.domain;

public class ObjectFactory {

  public static <T extends SizeAware> Object<T> create(ObjectPath objectPath, Binary<T> binary) {
    return Object.<T>builder()
      .objectPolicy(ObjectPolicy.ALL_POLICY)
      .objectSnapshot(ObjectSnapshot.initial())
      .objectPath(objectPath)
      .binary(binary)
      .objectPath(objectPath)
      .objectType(ObjectType.INITIAL)
      .size(0)
      .build();
  }
}
