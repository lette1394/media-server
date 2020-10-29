package io.lette1394.mediaserver.storage.domain

import io.lette1394.mediaserver.common.PolicyViolationException
import io.lette1394.mediaserver.storage.infrastructure.StringAware
import io.vavr.control.Try
import org.reactivestreams.Publisher
import spock.lang.Specification

import java.util.function.Function

import static io.lette1394.mediaserver.common.Violations.violation
import static io.lette1394.mediaserver.storage.domain.ObjectFixture.anyObject
import static io.lette1394.mediaserver.storage.domain.ObjectFixture.the

class ObjectTest extends Specification {
  def '정책을 어기면, 업로드 중단'() {
    given:
      ObjectPolicy forceReject = { snapshot -> Try.failure(violation("force to fail")) }
      BinaryPolicy anyBinaryPolicy = { throw new RuntimeException() }

      // TODO: Any, AnyAware
      Publisher<String> anyPublisher = null
      Function<String, StringAware> mapper = null

      Object object = anyObject(forceReject, anyBinaryPolicy, anyPublisher, mapper)
    when:
      def result = the object upload(null)
    then:
      thrown(OperationCanceled.class)

      // todo: assert inner exception type
//      expect object got Events.UploadRejected.class

      object.getEvents().size() == 1
      object.getEvents().get(0).getClass() == Events.UploadRejected.class
  }

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
