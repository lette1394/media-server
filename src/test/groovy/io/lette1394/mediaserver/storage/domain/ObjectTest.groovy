package io.lette1394.mediaserver.storage.domain


import io.lette1394.mediaserver.storage.BrokenIOException
import io.vavr.control.Try
import spock.lang.Specification

import java.util.concurrent.CompletionException

import static io.lette1394.mediaserver.common.Expects.expect
import static io.lette1394.mediaserver.storage.domain.ObjectEvents.UploadAborted
import static io.lette1394.mediaserver.storage.domain.ObjectEvents.UploadingTriggered
import static io.lette1394.mediaserver.storage.domain.ObjectFixture.*
import static io.lette1394.mediaserver.storage.domain.Violations.violation

class ObjectTest extends Specification {
  def '정책을 어기면, 업로드 중단'() {
    given:
      ObjectPolicy forceReject = { snapshot -> Try.failure(violation("force to fail")) }
      Object object = anyObject(forceReject)
    and:
      BinarySupplier randomBinary = randomBinarySupplier()
    when:
      def future = the object upload randomBinary
      future.join()
    then:
      thrown CompletionException
      expect future gotCause PolicyViolationException
      expect object got UploadAborted
  }

  def 'upstream 에서 예외가 발생하면, 업로드 중단'() {
    given:
      Object object = anyObject()
    and:
      BinarySupplier randomBinary = brokenBinarySupplier()
    when:
      def future = the object upload randomBinary
      future.join()
    then:
      thrown CompletionException
      expect future gotCause BrokenIOException
      expect object got([UploadingTriggered, UploadAborted])
  }
}
