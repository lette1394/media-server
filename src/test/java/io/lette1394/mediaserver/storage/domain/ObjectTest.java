package io.lette1394.mediaserver.storage.domain;

import static org.hamcrest.MatcherAssert.assertThat;

import io.lette1394.mediaserver.common.Event;
import io.lette1394.mediaserver.common.Result;
import io.lette1394.mediaserver.storage.InMemoryStorage;
import io.lette1394.mediaserver.storage.TestBinarySupplier;
import io.lette1394.mediaserver.storage.domain.ObjectEvents.UploadAborted;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.Test;

class ObjectTest {
  private final static String AREA_NAME = "TEST-AREA";
  private final static String OBJECT_KEY = "OBJECT_KEY_0001";

  private final static byte[] testBinary = RandomStringUtils.random(1000).getBytes();

  @Test
  void upload() {
    final ObjectFactory factory = new ObjectFactory(new InMemoryStorage(),
      current -> Result.fail("force fail"));
    final Object object = factory.create(AREA_NAME, OBJECT_KEY);

    object.upload(new TestBinarySupplier(testBinary)).join();

    assertThat(object, hasEvent(UploadAborted.class));
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
        description
          .appendText("object expected to have event:[%s], got:")
          .appendValue("none");
      }
    };
  }
}