package io.lette1394.mediaserver.matchers;

import io.lette1394.mediaserver.storage.domain.Command;
import io.lette1394.mediaserver.storage.domain.OperationCanceled;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class Matchers {
  public static <T extends Throwable> Matcher<Throwable> causeIs(Class<T> expected) {
    return new TypeSafeMatcher<>() {
      @Override
      protected boolean matchesSafely(Throwable item) {
        if (item.getCause() == null) {
          return false;
        }
        return item.getCause().getClass() == expected;
      }

      @Override
      public void describeTo(Description description) {
        description.appendValue(expected);
      }

      @Override
      protected void describeMismatchSafely(Throwable item, Description mismatchDescription) {
        mismatchDescription.appendValue(item.getCause());
      }
    };
  }

  public static <T extends OperationCanceled> Matcher<OperationCanceled> commandIs(Command expected) {
    return new TypeSafeMatcher<>() {
      @Override
      protected boolean matchesSafely(OperationCanceled item) {
        return item.getCommand() == expected;
      }

      @Override
      public void describeTo(Description description) {
        description.appendValue(expected).appendText(" in OperationCanceled");
      }

      @Override
      protected void describeMismatchSafely(OperationCanceled item, Description mismatchDescription) {
        mismatchDescription.appendValue(item.getCommand());
      }
    };
  }
}
