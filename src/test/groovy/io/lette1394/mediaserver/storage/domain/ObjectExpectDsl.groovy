package io.lette1394.mediaserver.storage.domain

import io.lette1394.mediaserver.common.Event

class ObjectExpectDsl {
  ObjectDsl objectDsl

  static expect(ObjectDsl objectDsl) {
    def dsl = new ObjectExpectDsl()
    dsl.objectDsl = objectDsl
    return dsl
  }

  void hasEvent(Class<? extends Event> eventType) {
    objectDsl.object.getEvents()
      .stream()
      .anyMatch({ event -> eventType.isInstance(event) })
  }
}
