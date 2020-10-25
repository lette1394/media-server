package io.lette1394.mediaserver.storage.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import io.lette1394.mediaserver.common.Event;
import io.lette1394.mediaserver.storage.domain.object.Object;
import java.util.List;
import java.util.stream.Collectors;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

class ObjectExpectDsl {
  Object object;

  private static <T> Matcher<T> sameClass(Class<T> expectedType) {
    return new TypeSafeMatcher<>() {
      @Override
      protected boolean matchesSafely(T item) {
        return item.getClass() == expectedType;
      }

      @Override
      public void describeTo(Description description) {
        description.appendValue(expectedType);
      }

      @Override
      protected void describeMismatchSafely(T item, Description mismatchDescription) {
        mismatchDescription.appendValue(item);
      }
    };
  }

  void got(Class<? extends Event> expectedType) {
    assertThat(object.getEvents(), hasSize(greaterThan(0)));
    assertThat(object.getEvents(), hasItem(is(sameClass(expectedType))));
  }

  <T extends Event> void got(List<Class<? extends T>> eventTypes) {
    final List<? extends Class<? extends Event>> actual = object.getEvents()
      .stream()
      .map(Event::getClass)
      .collect(Collectors.toList());

    assertThat(object.getEvents(), hasSize(eventTypes.size()));
    assertThat(actual, is(eventTypes));
  }
}
