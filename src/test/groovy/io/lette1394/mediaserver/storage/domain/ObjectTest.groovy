package io.lette1394.mediaserver.storage.domain

import io.lette1394.mediaserver.common.PolicyViolationException
import io.vavr.control.Try
import spock.lang.Specification

import static io.lette1394.mediaserver.common.Violations.violation
import static io.lette1394.mediaserver.storage.domain.ObjectFixture.anyObject
import static io.lette1394.mediaserver.storage.domain.ObjectFixture.the

class ObjectTest extends Specification {
//  def '정책을 어기면, 업로드 중단'() {
//    given:
//      ObjectPolicy forceReject = { snapshot -> Try.failure(violation("force to fail")) }
//      Object object = anyObject(forceReject)
//    when:
//      def result = the object upload()
//    then:
//      result.isLeft()
//      result.getLeft().getThrowable().getClass() == PolicyViolationException
//  }

//  def 'upstream 에서 예외가 발생하면, 업로드 중단'() {
//    given:
//      Object object = anyObject()
//    and:
//      BinarySupplier randomBinary = brokenBinarySupplier()
//    when:
//      def future = the object upload randomBinary
//      future.join()
//    then:
//      thrown CompletionException
//      expect future gotCause BrokenIOException
//      expect object got([UploadingTriggered, UploadAborted])
//  }
}
