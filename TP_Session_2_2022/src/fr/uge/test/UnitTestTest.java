package fr.uge.test;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class UnitTestTest {
  @Nested
  public class Q1 {
    @Test
    public void checkEqualsOk() {
      UnitTest.checkEquals(42, 42);
      UnitTest.checkEquals("42", "42");
    }

    @Test
    public void checkEqualsNotOk() {
      assertThrows(AssertionError.class, () -> UnitTest.checkEquals(42, "42"));
    }

    @Test
    public void checkEqualsErrorMessage() {
      try {
        UnitTest.checkEquals(42, -24);
      } catch(AssertionError error) {
        assertEquals("42 is not equal to -24", error.getMessage());
      }
    }

    @Test
    public void checkEqualsWorksWithNull() {
      UnitTest.checkEquals(null, null);
    }

    @Test
    public void checkEqualsWorksWithNull2() {
      assertThrows(AssertionError.class, () -> UnitTest.checkEquals(null, "42"));
      assertThrows(AssertionError.class, () -> UnitTest.checkEquals(42, null));
    }

    @Test
    public void oneTest() {
      var unitTest = new UnitTest();
      unitTest.test("name", () -> {});
      assertEquals(1, unitTest.testCount());
    }

    @Test
    public void severalTests() {
      var unitTest = new UnitTest();
      unitTest.test("test1", () -> {});
      unitTest.test("test2", () -> { throw null; });
      unitTest.test("test3", () ->  { UnitTest.checkEquals("foo", 3.14); });
      assertEquals(3, unitTest.testCount());
    }

    @Test
    public void testsWithSameName() {
      var unitTest = new UnitTest();
      unitTest.test("test1", () -> { });
      assertThrows(IllegalStateException.class, () -> unitTest.test("test1", () -> { }));
    }

    @Test
    public void implemantationHasOnlyOneField() {
      assertAll(
              () -> assertEquals(1, UnitTest.class.getDeclaredFields().length),
              () -> assertEquals(Object.class, UnitTest.class.getSuperclass())
      );
    }

    @Test
    public void preconditions() {
      var unitTest = new UnitTest();
      assertAll(
              () -> assertThrows(NullPointerException.class, () -> unitTest.test(null, () -> { })),
              () -> assertThrows(NullPointerException.class, () -> unitTest.test("this is a test", null))
      );
    }
  }


  @Nested
  public class Q2 {
    @Test
    public void runOnly() {
      var unitTest = new UnitTest();
      unitTest.test("test 1",
              () -> UnitTest.checkEquals(42, 42));
      List<Error> errors = unitTest.runOnly("test 1");
      assertEquals(List.of(), errors);
    }

    @Test
    public void runOnlyFail() {
      var unitTest = new UnitTest();
      unitTest.test("test 1",
              () -> UnitTest.checkEquals(41.8, "banana"));
      var errors = unitTest.runOnly("test 1");
      assertAll(
              () -> assertEquals(1, errors.size()),
              () -> assertEquals("41.8 is not equal to banana", errors.get(0).getMessage())
      );
    }

    @Test
    public void aLotOfTests() {
      var unitTest = new UnitTest();
      IntStream.range(0, 1_000_000).forEach(i -> unitTest.test("" + i, () -> {}));
      assertTrue(IntStream.range(0, 1_000_000).allMatch(i -> unitTest.runOnly("" + i).isEmpty()));
    }

    @Test
    public void preconditions() {
      var unitTest = new UnitTest();
      unitTest.test("test 1",
              () -> UnitTest.checkEquals(41.8, "banana"));
      assertAll(
              () -> assertThrows(NullPointerException.class, () -> unitTest.runOnly(null)),
              () -> assertThrows(IllegalStateException.class, () -> unitTest.runOnly("another test"))
      );
    }
  }

  /*
  @Nested
  public class Q3 {
    @Test
    public void runOnlyWithARuntimeException() {
      var unitTest = new UnitTest();
      unitTest.test("test npe", () -> ((String) null).toString());
      var errors = unitTest.runOnly("test npe");
      assertAll(
              () -> assertEquals(1, errors.size()),
              () -> assertEquals("java.lang.NullPointerException: Cannot invoke \"String.toString()\" because \"null\" is null", errors.get(0).getMessage())
      );
    }

    @Test
    public void runOnlyWithAnOutOfMemoryError() {
      var unitTest = new UnitTest();
      unitTest.test("test memory", () -> new long[Integer.MAX_VALUE].toString());
      var errors = unitTest.runOnly("test memory");
      assertAll(
              () -> assertEquals(1, errors.size()),
              () -> assertEquals("Requested array size exceeds VM limit", errors.get(0).getMessage())
      );
    }

    @Test
    public void aLotOfFailedTests() {
      var unitTest = new UnitTest();
      IntStream.range(0, 1_000_000).forEach(i -> unitTest.test("" + i, () -> ((String) null).toString()));
      assertTrue(IntStream.range(0, 1_000_000).allMatch(i -> unitTest.runOnly("" + i).size() == 1));
    }
  }

  @Nested
  public class Q4 {
    @Test
    public void ensure() {
      assertAll(
              () -> UnitTest.ensure(42).equalsTo(42),
              () -> UnitTest.ensure("foo").equalsTo("foo"),
              () -> UnitTest.ensure(3.14).equalsTo(3.14)
      );
    }

    @Test
    public void ensureFail() {
      assertAll(
              () -> assertThrows(AssertionError.class, () -> UnitTest.ensure(1).equalsTo(2)),
              () -> assertThrows(AssertionError.class, () -> UnitTest.ensure("foo").equalsTo("bar")),
              () -> assertThrows(AssertionError.class, () -> UnitTest.ensure(3.0).equalsTo(4.0))
      );
    }

    @Test
    public void ensureErrorMessage() {
      try {
        UnitTest.ensure(1).equalsTo(2);
      } catch (AssertionError error) {
        assertEquals("1 is not equal to 2", error.getMessage());
      }
    }

    @Test
    public void ensureWorksWithNull() {
      assertAll(
              () -> UnitTest.ensure((Object) null).equalsTo(null),
              () -> assertThrows(AssertionError.class, () -> UnitTest.ensure((String) null).equalsTo("bar")),
              () -> assertThrows(AssertionError.class, () -> UnitTest.ensure("bar").equalsTo(null))
      );
    }

    @Test
    public void ensureIsParametric() {
      UnitTest.Ensure<String> ensureString = UnitTest.ensure("foo");
      assertNotNull(ensureString);
      UnitTest.Ensure<Integer> ensureInteger = UnitTest.ensure(42);
      assertNotNull(ensureInteger);
      UnitTest.Ensure<Double> ensureDouble = UnitTest.ensure(3.14);
      assertNotNull(ensureDouble);
    }
  }

  @Nested
  public class Q5 {
    @Test
    public void ensureNot() {
      assertAll(
              () -> UnitTest.ensure(42).not().equalsTo(41),
              () -> UnitTest.ensure("foo").not().equalsTo("bar"),
              () -> UnitTest.ensure(3.14).not().equalsTo(14.0)
      );
    }

    @Test
    public void ensureNotFail() {
      assertAll(
              () -> assertThrows(AssertionError.class, () -> UnitTest.ensure(1).not().equalsTo(1)),
              () -> assertThrows(AssertionError.class, () -> UnitTest.ensure("foo").not().equalsTo("foo")),
              () -> assertThrows(AssertionError.class, () -> UnitTest.ensure(4.0).not().equalsTo(4.0))
      );
    }

    @Test
    public void ensureNotErrorMessage() {
      try {
        UnitTest.ensure(1).not().equalsTo(1);
      } catch (AssertionError error) {
        assertEquals("1 is equal to 1", error.getMessage());
      }
    }

    @Test
    public void ensureNotNot() {
      assertAll(
              () -> UnitTest.ensure(42).not().not().equalsTo(42),
              () -> UnitTest.ensure("foo").not().not().equalsTo("foo"),
              () -> UnitTest.ensure(3.14).not().not().equalsTo(3.14)
      );
    }

    @Test
    public void ensureNotNotFail() {
      assertAll(
              () -> assertThrows(AssertionError.class, () -> UnitTest.ensure(1).not().not().equalsTo(2)),
              () -> assertThrows(AssertionError.class, () -> UnitTest.ensure("foo").not().not().equalsTo("bar")),
              () -> assertThrows(AssertionError.class, () -> UnitTest.ensure(3.0).not().not().equalsTo(4.0))
      );
    }

    @Test
    public void ensureNotWorksWithNull() {
      assertAll(
              () -> UnitTest.ensure((String) null).not().equalsTo("bar"),
              () -> UnitTest.ensure("bar").not().equalsTo(null),
              () -> assertThrows(AssertionError.class, () -> UnitTest.ensure((Object) null).not().equalsTo(null))
      );
    }

    @Test
    public void ensureNotIsParametric() {
      UnitTest.Ensure<String> ensureString = UnitTest.ensure("foo").not();
      assertNotNull(ensureString);
      UnitTest.Ensure<Integer> ensureInteger = UnitTest.ensure(42).not();
      assertNotNull(ensureInteger);
      UnitTest.Ensure<Double> ensureDouble = UnitTest.ensure(3.14).not();
      assertNotNull(ensureDouble);
    }

    @Test
    public void ensureNotIsAnInterface() throws NoSuchMethodException {
      assertTrue(UnitTest.Ensure.class.getMethod("not").getReturnType().isInterface());
      assertTrue(Modifier.isPublic(UnitTest.Ensure.class.getMethod("not").getReturnType().getModifiers()));
    }
  }

  @Nested
  public class Q6 {
    @Test
    public void ensureWithAReturnValue() {
      UnitTest.ensureCode(() -> 42).returnValue().equalsTo(42);
      UnitTest.ensureCode(() -> 42).returnValue().not().equalsTo(43);
    }

    @Test
    public void ensureWithAReturnValueFail() {
      var unitTest = new UnitTest();
      unitTest.test("test",
              () -> UnitTest.ensureCode(() -> 42).returnValue().equalsTo(43));
      var errors = unitTest.runOnly("test");
      assertAll(
              () -> assertEquals(1, errors.size()),
              () -> assertEquals("42 is not equal to 43", errors.get(0).getMessage())
      );
    }

    @Test
    public void ensureWithAReturnValuePropagateNull() {
      var unitTest = new UnitTest();
      unitTest.test("test",
              () -> UnitTest.ensureCode(() -> null).returnValue().equalsTo(null));
      var errors = unitTest.runOnly("test");
      assertEquals(List.of(), errors);
    }

    @Test
    public void ensureWithAReturnValueAndNotFail() {
      var unitTest = new UnitTest();
      unitTest.test("test",
              () -> UnitTest.ensureCode(() -> 42).returnValue().not().equalsTo(42));
      var errors = unitTest.runOnly("test");
      assertAll(
              () -> assertEquals(1, errors.size()),
              () -> assertEquals("42 is equal to 42", errors.get(0).getMessage())
      );
    }

    @Test
    public void ensureWithAReturnWhileThrowingAnIOException() {
      var unitTest = new UnitTest();
      unitTest.test("test",
              () -> UnitTest.ensureCode(() -> { throw new IOException("oops"); }).returnValue().equalsTo(42));
      var errors = unitTest.runOnly("test");
      assertAll(
              () -> assertEquals(1, errors.size()),
              () -> assertEquals("java.io.IOException: oops", errors.get(0).getMessage())
      );
    }
  }

  @Nested
  public class Q7 {
    @Test
    public void ensureWithAnException() {
      UnitTest.ensureCode(() -> ((String) null).toString()).throwsAnException(NullPointerException.class);
    }

    @Test
    public void ensureFailToThrowAnException() {
      var unitTest = new UnitTest();
      unitTest.test("test",
              () -> UnitTest.ensureCode(() -> 42).throwsAnException(RuntimeException.class)
      );
      var errors = unitTest.runOnly("test");
      assertAll(
              () -> assertEquals(1, errors.size()),
              () -> assertEquals("expect java.lang.RuntimeException but no exception was thrown", errors.get(0).getMessage())
      );
    }

    @Test
    public void ensureFailThrowsTheWrongException() {
      var unitTest = new UnitTest();
      unitTest.test("test",
              () -> UnitTest.ensureCode(() -> (new Object[0])[0]).throwsAnException(NullPointerException.class)
      );
      var errors = unitTest.runOnly("test");
      assertAll(
              () -> assertEquals(1, errors.size()),
              () -> assertEquals("unexpected exception java.lang.ArrayIndexOutOfBoundsException", errors.get(0).getMessage())
      );
    }

    @Test
    public void ensureFailThrowsTheWrongException2() {
      var unitTest = new UnitTest();
      unitTest.test("test",
              () -> UnitTest.ensureCode(() -> { throw new IOException("oops"); }).throwsAnException(NullPointerException.class)
      );
      var errors = unitTest.runOnly("test");
      assertAll(
              () -> assertEquals(1, errors.size()),
              () -> assertEquals("unexpected exception java.io.IOException", errors.get(0).getMessage())
      );
    }

    @Test
    public void precondition() {
      var box = new Object() { boolean oops; };
      assertThrows(NullPointerException.class, () -> UnitTest.ensureCode(() -> box.oops = true).throwsAnException(null));
      assertFalse(box.oops);
    }
  }

  @Nested
  public class Q8 {
    @Test
    public void report() {
      var all = List.of("test1", "test2");
      var error = new Error("error");
      var errors = Map.of("test1", List.of(error));
      var report = new UnitTest.Report(all, errors);
      assertAll(
              () -> assertEquals(List.of("test1", "test2"), report.names()),
              () -> assertEquals(Map.of("test1", List.of(error)), report.errors())
      );
    }

    @Test
    public void report2() {
      var all = new ArrayList<String>(List.of("test1", "test2"));
      var error = new Error("error");
      var errors = new HashMap<>(Map.of("test1", List.of(error)));
      var report = new UnitTest.Report(all, errors);
      assertAll(
              () -> assertEquals(List.of("test1", "test2"), report.names()),
              () -> assertEquals(Map.of("test1", List.of(error)), report.errors())
      );
    }

    @Test
    public void reportAllAndErrorsNonMutable() {
      var all = new ArrayList<String>(List.of("test1", "test2"));
      var errors = new HashMap<>(Map.of("test1", List.of(new Error("error"))));
      var report = new UnitTest.Report(all, errors);
      assertAll(
              () -> assertThrows(UnsupportedOperationException.class, () -> report.names().add("foo")),
              () -> assertThrows(UnsupportedOperationException.class, () -> report.errors().put("foo", List.of()))
      );
    }

    @Test
    public void reportCanNotBeChangedByChangingTheInitialData() {
      var all = new ArrayList<String>(List.of("test1", "test2"));
      var error = new Error("error");
      var errors = new HashMap<>(Map.of("test1", List.of(error)));
      var report = new UnitTest.Report(all, errors);
      all.add("test3");
      errors.put("test3", List.of());
      assertAll(
              () -> assertEquals(List.of("test1", "test2"), report.names()),
              () -> assertEquals(Map.of("test1", List.of(error)), report.errors())
      );
    }

    @Test
    public void preconditions() {
      assertAll(
              () -> assertThrows(NullPointerException.class, () -> new UnitTest.Report(null, Map.of())),
              () -> assertThrows(NullPointerException.class, () -> new UnitTest.Report(List.of(), null))
      );
    }
  }

  @Nested
  public class Q9 {
    @Test
    public void runAll() {
      var unitTest = new UnitTest();
      unitTest.test("test1", () -> {});
      unitTest.test("test2", () -> {});
      var report = unitTest.runAll();
      assertAll(
              () -> assertEquals(List.of("test1", "test2"), report.names()),
              () -> assertEquals(Map.of("test1", List.of(), "test2", List.of()), report.errors())
      );
    }

    @Test
    public void runAllWithFailure() {
      var unitTest = new UnitTest();
      unitTest.test("test1", () -> {});
      unitTest.test("test2", () -> UnitTest.checkEquals("foo", "bar"));
      var report = unitTest.runAll();
      assertAll(
              () -> assertEquals(List.of("test1", "test2"), report.names()),
              () -> assertEquals(List.of(), report.errors().get("test1")),
              () -> assertEquals(1, report.errors().get("test2").size()),
              () -> assertTrue(report.errors().get("test2").get(0) instanceof AssertionError)
      );
    }

    @Test
    public void multipleTests() {
      var unitTest = new UnitTest();
      unitTest.test("test",
              () -> {},
              () -> {}
      );
      var report = unitTest.runAll();
      assertAll(
              () -> assertEquals(List.of("test"), report.names()),
              () -> assertEquals(Map.of("test", List.of()), report.errors())
      );
    }

    @Test
    public void multipleTestsWithFailure() {
      var unitTest = new UnitTest();
      unitTest.test("test",
              () -> {},
              () -> UnitTest.checkEquals("foo", "bar")
      );
      var report = unitTest.runAll();
      assertAll(
              () -> assertEquals(List.of("test"), report.names()),
              () -> assertEquals(1, report.errors().get("test").size()),
              () -> assertEquals("foo is not equal to bar", report.errors().get("test").get(0).getMessage())
      );
    }

    @Test
    public void aLotOfTests() {
      var unitTest = new UnitTest();
      IntStream.range(0, 1_000_000).forEach(i -> unitTest.test("" + i, () -> {}));
      var report = unitTest.runAll();
      assertAll(
              () -> assertEquals(1_000_000, report.names().size()),
              () -> assertEquals(1_000_000, report.errors().size()),
              () -> assertTrue(report.errors().values().stream().allMatch(List::isEmpty))
      );
    }

    @Test
    public void aLotOfFailedTests() {
      var unitTest = new UnitTest();
      IntStream.range(0, 1_000_000).forEach(i -> unitTest.test("" + i, () -> ((String) null).toString()));
      var report = unitTest.runAll();
      assertAll(
              () -> assertEquals(1_000_000, report.names().size()),
              () -> assertTrue(report.errors().values().stream().allMatch(errors -> errors.size() == 1))
      );
    }

    @Test
    public void multipleTestsWithFailureRunOnly() {
      var unitTest = new UnitTest();
      unitTest.test("test",
              () -> {},
              () -> UnitTest.checkEquals("foo", "bar"),
              () -> UnitTest.checkEquals(12, 24)
      );
      var errors = unitTest.runOnly("test");
      assertAll(
              () -> assertEquals(2, errors.size()),
              () -> assertEquals("foo is not equal to bar", errors.get(0).getMessage()),
              () -> assertEquals("12 is not equal to 24", errors.get(1).getMessage())
      );
    }
  }
  */
}