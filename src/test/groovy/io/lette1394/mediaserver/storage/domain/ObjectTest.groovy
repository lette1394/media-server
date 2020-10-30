package io.lette1394.mediaserver.storage.domain


import io.lette1394.mediaserver.common.PolicyViolationException
import io.vavr.control.Try
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

import static io.lette1394.mediaserver.common.Expects.expect
import static io.lette1394.mediaserver.common.Violations.violation
import static io.lette1394.mediaserver.storage.domain.Events.*
import static io.lette1394.mediaserver.storage.domain.ObjectFixture.anyObject
import static io.lette1394.mediaserver.storage.domain.ObjectFixture.anyBinary
import static io.lette1394.mediaserver.storage.domain.ObjectFixture.the

class ObjectTest extends Specification {
  def '정책을 어기면, 업로드 중단'() {
    given:
      ObjectPolicy forceReject = { snapshot -> Try.failure(violation("force to fail")) }
      Object object = anyObject(forceReject)
    when:
      the object upload(anyBinary())
    then:
      thrown(OperationCanceled.class)

      expect object got UploadRejected.class
      expect object gotCause PolicyViolationException.class
  }

//  def 'upstream 에서 예외가 발생하면, 업로드 중단'() {
//    given:
//      Object object = anyObject()
//    when:
//      def binarySupplier = the object upload(anyBinary())
//
//    then:
//      thrown(OperationCanceled.class)
//
//      expect object got UploadRejected.class
//      expect object gotCause PolicyViolationException.class
//  }
}
