package fr.uge.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class UnitTest {
  private final HashMap<String, Runnable> tests = new HashMap<>();
  public static void checkEquals(Object first, Object second) {
    if (!Objects.equals(first, second)) {
      throw new AssertionError(first + " is not equal to " + second);
    }
  }

  public void test(String testName, Runnable runnable) {
    Objects.requireNonNull(testName);
    Objects.requireNonNull(runnable);
    if (tests.containsKey(testName)) {
      throw new IllegalStateException();
    }

    tests.put(testName, runnable);
  }

  public List<Error> runOnly(String testName) {
    Objects.requireNonNull(testName);
    if (!tests.containsKey(testName)) {
      throw new IllegalStateException();
    }

    var errors = new ArrayList<Error>();
    try {
      tests.get(testName).run();
    } catch (Error error) {
      errors.add(new Error(error.getMessage()));
    } catch (Throwable throwable) {
      errors.add(new AssertionError(throwable));
    }

    return List.copyOf(errors);
  }

  public int testCount() {
    return tests.size();
  }
}
