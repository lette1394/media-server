package io.lette1394.mediaserver.matchers;

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

  public static <T extends Event, BUFFER extends Payload> Matcher<Object<BUFFER>> got(
    Class<T> expected) {
    return new TypeSafeMatcher<>() {
      @Override
      protected boolean matchesSafely(Object<BUFFER> item) {
        return item.getEvents()
          .stream()
          .anyMatch(event -> event.getClass() == expected);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("got event: ").appendValue(expected);
      }

      @Override
      protected void describeMismatchSafely(Object<BUFFER> item, Description mismatchDescription) {
        final List<? extends Class<? extends Event>> collect = item.getEvents()
          .stream()
          .map(Event::getClass)
          .collect(Collectors.toList());
        mismatchDescription.appendValue(collect);
      }
    };
  }

  public static <T extends Event, BUFFER extends Payload> Matcher<Object<BUFFER>> got(
    List<Class<? extends T>> expected) {

    return new TypeSafeMatcher<>() {
      @Override
      protected boolean matchesSafely(Object<BUFFER> item) {
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
      protected void describeMismatchSafely(Object<BUFFER> item, Description mismatchDescription) {
        final List<? extends Class<? extends Event>> eventTypes = item.getEvents()
          .stream()
          .map(event -> event.getClass())
          .collect(Collectors.toList());
        mismatchDescription.appendValue(eventTypes);
      }
    };
  }

  public static <T extends Event, BUFFER extends Payload> Matcher<Object<BUFFER>> is(ObjectType expected) {

    return new TypeSafeMatcher<>() {
      @Override
      protected boolean matchesSafely(Object<BUFFER> item) {
        return item.getType().is(expected);
      }

      @Override
      public void describeTo(Description description) {
        description.appendValue(expected);
      }

      @Override
      protected void describeMismatchSafely(Object<BUFFER> item, Description mismatchDescription) {
        mismatchDescription.appendValue(expected);
      }
    };
  }
}
