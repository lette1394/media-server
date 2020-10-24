package io.lette1394.mediaserver.storage.domain


import io.vavr.control.Try
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

import static io.lette1394.mediaserver.common.CompletableFutureExpectDsl.expect as expectFuture
import static io.lette1394.mediaserver.storage.domain.ObjectDsl.*
import static io.lette1394.mediaserver.storage.domain.ObjectEvents.UploadAborted
import static io.lette1394.mediaserver.storage.domain.ObjectExpectDsl.expect
import static io.lette1394.mediaserver.storage.domain.ObjectFixture.*
import static io.lette1394.mediaserver.storage.domain.Violations.violation

class ObjectTest extends Specification {
  def 'test'() {
    given:
      ObjectPolicy FORCE_REJECT = { snapshot -> Try.failure(violation("force to fail")) }
      ObjectDsl object = aInitialObject() with anyIdentifier() obey FORCE_REJECT resideIn memory()
    and:
      BinarySupplier randomBinary = randomBinarySupplier()
    when:
      CompletableFuture<Object> result = the object got uploading() from randomBinary
    then:
      expect object hasEvent UploadAborted
      expectFuture result completedExceptionallyWith PolicyViolationException.class
  }
}
