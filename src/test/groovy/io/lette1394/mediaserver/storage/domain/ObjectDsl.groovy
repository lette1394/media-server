package io.lette1394.mediaserver.storage.domain

import java.util.concurrent.CompletableFuture
import java.util.function.BiFunction

class ObjectDsl {
  Identifier identifier
  ObjectState state
  ObjectPolicy policy
  BinaryRepository binaryRepository
  BiFunction<Object, BinarySupplier, CompletableFuture<Object>> method

  Object object
  CompletableFuture<Object> future

  static ObjectDsl the(ObjectDsl objectDsl) {
    return objectDsl
  }

  static ObjectDsl aInitialObject() {
    final ObjectDsl objectDsl = new ObjectDsl()
    objectDsl.state = ObjectState.INITIAL
    return objectDsl
  }

  ObjectDsl with(Identifier identifier) {
    this.identifier = identifier
    return this
  }

  ObjectDsl obey(ObjectPolicy objectPolicy) {
    this.policy = objectPolicy
    return this
  }

  ObjectDsl resideIn(BinaryRepository binaryRepository) {
    this.binaryRepository = binaryRepository
    return this
  }

  ObjectDsl got(BiFunction<Object, BinarySupplier, CompletableFuture<Object>> method) {
    this.method = method
    return this
  }

  static BiFunction<Object, BinarySupplier, CompletableFuture<Object>> uploading() {
    return {
      object, binarySupplier -> object.upload(binarySupplier)
    }
  }

  CompletableFuture<Object> from(BinarySupplier binarySupplier) {
    object = toObject(binaryRepository)
    return method.apply(toObject(binaryRepository), binarySupplier)
  }

  private Object toObject(BinaryRepository binaryRepository) {
    return new ObjectFactory(binaryRepository, policy)
      .create(identifier.area, identifier.key)
  }

  Object go() {
    return object
  }
}
