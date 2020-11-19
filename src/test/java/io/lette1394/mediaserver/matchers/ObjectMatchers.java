package io.lette1394.mediaserver.matchers;

import com.google.common.collect.Lists;
import io.lette1394.mediaserver.common.Event;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectType;
import io.lette1394.mediaserver.storage.domain.Payload;
import java.util.List;
import java.util.stream.Collectors;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class ObjectMatchers {

  public static <T extends Event, P extends Payload>
  Matcher<Object<P>> got(Class<T> expected) {
    return new TypeSafeMatcher<>() {
      @Override
      protected boolean matchesSafely(Object<P> item) {
        return item.getEvents()
          .stream()
          .anyMatch(event -> event.getClass() == expected);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("got event: ").appendValue(expected);
      }

      @Override
      protected void describeMismatchSafely(Object<P> item, Description mismatchDescription) {
        final List<? extends Class<? extends Event>> collect = item.getEvents()
          .stream()
          .map(Event::getClass)
          .collect(Collectors.toList());
        mismatchDescription.appendValue(collect);
      }
    };
  }

  public static <T extends Event, P extends Payload>
  Matcher<Object<P>> got(List<Class<? extends T>> expected) {
    return new TypeSafeMatcher<>() {
      @Override
      protected boolean matchesSafely(Object<P> item) {
        final List<? extends Class<? extends Event>> eventTypes = item.getEvents()
          .stream()
          .map(event -> event.getClass())
          .collect(Collectors.toList());

        return eventTypes.equals(expected);
      }

      @Override
      public void describeTo(Description description) {
        description.appendValue(expected);
      }

      @Override
      protected void describeMismatchSafely(Object<P> item, Description mismatchDescription) {
        final List<? extends Class<? extends Event>> eventTypes = item.getEvents()
          .stream()
          .map(event -> event.getClass())
          .collect(Collectors.toList());
        mismatchDescription.appendValue(eventTypes);
      }
    };
  }

  public static <T extends Event, P extends Payload>
  Matcher<Object<P>> hasType(ObjectType expected) {
    return new TypeSafeMatcher<>() {
      @Override
      protected boolean matchesSafely(Object<P> item) {
        return item.getObjectType().is(expected);
      }

      @Override
      public void describeTo(Description description) {
        description.appendValue(expected);
      }

      @Override
      protected void describeMismatchSafely(Object<P> item, Description mismatchDescription) {
        mismatchDescription.appendValue(item.getObjectType());
      }
    };
  }

  public static <T extends Event, P extends Payload>
  Matcher<Object<P>> hasSize(long expected) {
    return new TypeSafeMatcher<>() {
      @Override
      protected boolean matchesSafely(Object<P> item) {
        return item.getSize() == expected;
      }

      @Override
      public void describeTo(Description description) {
        description.appendValue(expected);
      }

      @Override
      protected void describeMismatchSafely(Object<P> item, Description mismatchDescription) {
        mismatchDescription.appendValue(item.getSize());
      }
    };
  }

  public static <T extends Event, P extends Payload>
  Matcher<Object<P>> hasProgressingSize(long expected) {
    return new TypeSafeMatcher<>() {
      @Override
      protected boolean matchesSafely(Object<P> item) {
        return item.getProgressingSize() == expected;
      }

      @Override
      public void describeTo(Description description) {
        description.appendValue(expected);
      }

      @Override
      protected void describeMismatchSafely(Object<P> item, Description mismatchDescription) {
        mismatchDescription.appendValue(item.getProgressingSize());
      }
    };
  }

  @SafeVarargs
  public static <T extends Class<? extends Event>> List<T> events(T... events) {
    return Lists.newArrayList(events);
  }
}
