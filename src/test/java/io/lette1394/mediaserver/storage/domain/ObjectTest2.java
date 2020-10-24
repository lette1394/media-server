package io.lette1394.mediaserver.storage.domain;

import static io.lette1394.mediaserver.storage.domain.Violations.violation;
import static org.hamcrest.MatcherAssert.assertThat;

import io.lette1394.mediaserver.common.Event;
import io.lette1394.mediaserver.storage.InMemoryStorage;
import io.lette1394.mediaserver.storage.TestBinarySupplier;
import io.lette1394.mediaserver.storage.domain.ObjectEvents.UploadAborted;
import io.lette1394.mediaserver.storage.domain.ObjectEvents.Uploaded;
import io.vavr.control.Try;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.Test;

class ObjectTest2 {
  private final static String AREA_NAME = "TEST-AREA";
  private final static String OBJECT_KEY = "OBJECT_KEY_0001";

  private final static byte[] testBinary = RandomStringUtils.random(1000).getBytes();

  private final ObjectPolicy ALLOW_ALL = current -> Try.success(null);
  private final ObjectPolicy REJECT_ALL = current -> Try.failure(violation("force to fail"));
  private final Storage inMemory = new InMemoryStorage();

  @Test
  void upload() {
    final Object object = object(REJECT_ALL);
    final CompletableFuture<Object> future = object.upload(new TestBinarySupplier(testBinary));

    assertThat(future, joinedExceptionallyWith(PolicyViolationException.class));
    assertThat(object, hasEvent(UploadAborted.class));
  }

  @Test
  void upload2() {
    final Object object = object(ALLOW_ALL)
      .upload(new TestBinarySupplier(testBinary))
      .join();

    assertThat(object, hasEvent(Uploaded.class));
  }

  private Object object(ObjectPolicy objectPolicy) {
    final ObjectFactory factory = new ObjectFactory(inMemory, objectPolicy);
    return factory.create(AREA_NAME, OBJECT_KEY);
  }

  private <T extends Event> Matcher<CompletableFuture<?>> joinedExceptionallyWith(
    Class<? extends Throwable> expected) {
    return new TypeSafeMatcher<>() {
      @Override
      protected boolean matchesSafely(CompletableFuture<?> item) {
        try {
          item.join();
          return false;
        } catch (Exception e) {
          if (e instanceof CompletionException) {
            return e.getCause() != null && e.getCause().getClass() == expected;
          }
        }
        return false;
      }

      @Override
      public void describeTo(Description description) {
        description
          .appendText("CompletableFuture expected to be completed exceptionally with:")
          .appendValue(expected);
      }

      @Override
      protected void describeMismatchSafely(CompletableFuture<?> item,
        Description mismatchDescription) {
        mismatchDescription.appendValue(item);
      }
    };
  }

  private <T extends Event> Matcher<Object> hasEvent(Class<T> eventType) {
    return new TypeSafeMatcher<>() {
      @Override
      protected boolean matchesSafely(Object item) {
        return item.getEvents()
          .stream()
          .anyMatch(eventType::isInstance);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("a Object contains domain event ").appendValue(eventType);
      }

      @Override
      protected void describeMismatchSafely(Object item, Description mismatchDescription) {
        final List<Event> events = item.getEvents();
        mismatchDescription
          .appendText("got ")
          .appendValue(events.size())
          .appendText(" events, which are ")
          .appendValue(item.getEvents());
      }
    };
  }
}