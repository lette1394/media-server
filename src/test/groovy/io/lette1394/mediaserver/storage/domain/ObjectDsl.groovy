package io.lette1394.mediaserver.storage.domain

import io.lette1394.mediaserver.storage.domain.binary.BinaryRepository
import io.lette1394.mediaserver.storage.domain.binary.BinarySupplier
import io.lette1394.mediaserver.storage.domain.object.Identifier
import io.lette1394.mediaserver.storage.domain.object.Object
import io.lette1394.mediaserver.storage.domain.object.Factory
import io.lette1394.mediaserver.storage.domain.object.Policy
import io.lette1394.mediaserver.storage.domain.object.State

import java.util.concurrent.CompletableFuture
import java.util.function.BiFunction

class ObjectDsl {
  Identifier identifier
  State state
  Policy policy
  BinaryRepository binaryRepository
  BiFunction<Object, BinarySupplier, CompletableFuture<Object>> method

  Object object
  CompletableFuture<Object> future

  static ObjectDsl the(ObjectDsl objectDsl) {
    return objectDsl
  }

  static ObjectDsl aInitialObject() {
    final ObjectDsl objectDsl = new ObjectDsl()
    objectDsl.state = State.INITIAL
    return objectDsl
  }

  ObjectDsl with(Identifier identifier) {
    this.identifier = identifier
    return this
  }

  ObjectDsl obey(Policy objectPolicy) {
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
    return new Factory(binaryRepository, policy)
      .create(identifier.area, identifier.key)
  }

  Object go() {
    return object
  }
}
