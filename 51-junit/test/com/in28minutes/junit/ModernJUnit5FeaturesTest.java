package com.in28minutes.junit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.Duration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * A quick tour of the JUnit Jupiter features that the original "first 5 steps"
 * lecture doesn't cover.
 *
 * Every test here runs against the existing MyMath.calculateSum(int[]) method.
 * Nothing new was added to MyMath - the whole point is to show how much you can
 * exercise with just the annotations and the assertions library.
 *
 * What's on display:
 *   - @DisplayName        : readable names in the test report
 *   - @ParameterizedTest  : run one test with many inputs (@ValueSource, @CsvSource)
 *   - assertThrows        : check that code throws the exception we expect
 *   - assertTimeout       : fail the test if it runs too slowly
 *   - assumeTrue          : skip a test when a precondition isn't met
 *   - @RepeatedTest       : run the same test N times
 *   - @Disabled           : park a test for now (always leave a reason)
 *   - @Nested             : group related tests inside an outer class
 *   - @Tag                : label tests so the build can include or skip them
 *
 * Docs: https://docs.junit.org/current/user-guide/#writing-tests
 */
@DisplayName("Modern JUnit 5/6 features - quick tour")
class ModernJUnit5FeaturesTest {

	private final MyMath math = new MyMath();

	// ------------------------------------------------------------------
	// @ParameterizedTest: same test body, many inputs.
	// ------------------------------------------------------------------

	@ParameterizedTest(name = "an array of {0} zeros still sums to 0")
	@ValueSource(ints = {0, 1, 5, 50})
	void calculateSum_arraysOfZeros(int length) {
		assertEquals(0, math.calculateSum(new int[length]));
	}

	// @CsvSource lets us pass the input array (as a String we split) and the
	// expected sum together on one line. Reads almost like a little table.
	@ParameterizedTest(name = "sum of [{0}] = {1}")
	@CsvSource({
		"'1,2,3',     6",
		"'5',         5",
		"'-1,-2,-3', -6",
		"'10,-10',    0"
	})
	void calculateSum_csvSource(String csv, int expected) {
		int[] numbers = toIntArray(csv);
		assertEquals(expected, math.calculateSum(numbers));
	}

	// ------------------------------------------------------------------
	// assertThrows: calculateSum(null) walks a null array in its for-each,
	// which throws NullPointerException. No extra production method needed -
	// the existing code already gives us an exception to assert on.
	// ------------------------------------------------------------------

	@Test
	@DisplayName("calculateSum(null) throws NullPointerException")
	void calculateSum_null_throws() {
		assertThrows(NullPointerException.class, () -> math.calculateSum(null));
	}

	// ------------------------------------------------------------------
	// assertTimeout: the sum of a small array should be near instant.
	// ------------------------------------------------------------------

	@Test
	void calculateSum_isFast() {
		assertTimeout(Duration.ofMillis(100),
			() -> math.calculateSum(new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}));
	}

	// ------------------------------------------------------------------
	// Assumptions: skip (not fail) the test when a precondition isn't met.
	// Handy for tests that only make sense on a 64-bit JVM, on Linux, etc.
	// ------------------------------------------------------------------

	@Test
	void runsOnlyOn64BitJvm() {
		assumeTrue("64".equals(System.getProperty("sun.arch.data.model")),
			"Skipping - this test only makes sense on a 64-bit JVM");
		assertNotNull(math);
	}

	// ------------------------------------------------------------------
	// @RepeatedTest: run the same test a few times (good for chasing flakes).
	// ------------------------------------------------------------------

	@RepeatedTest(value = 3, name = "calculateSum stays deterministic - run {currentRepetition} of {totalRepetitions}")
	void calculateSum_repeated() {
		assertEquals(6, math.calculateSum(new int[] {1, 2, 3}));
	}

	// ------------------------------------------------------------------
	// @Disabled: switch a test off without deleting it. Always say why.
	// ------------------------------------------------------------------

	@Test
	@Disabled("Demo only - shows how to temporarily skip a test")
	void thisTestIsCurrentlySkipped() {
		throw new IllegalStateException("Should never run");
	}

	// ------------------------------------------------------------------
	// @Nested: keep related cases together under one readable heading.
	// ------------------------------------------------------------------

	@Nested
	@DisplayName("calculateSum edge cases")
	class CalculateSumEdgeCases {

		@Test
		void emptyArray_returnsZero() {
			assertEquals(0, math.calculateSum(new int[] {}));
		}

		@Test
		void singleElement_returnsThatElement() {
			assertEquals(42, math.calculateSum(new int[] {42}));
		}

		@Test
		void negativeNumbers_areSummedCorrectly() {
			assertEquals(-6, math.calculateSum(new int[] {-1, -2, -3}));
		}
	}

	// ------------------------------------------------------------------
	// @Tag: label a test so the build can include or exclude it, e.g.
	// mvn test -Dgroups=fast, or wire it up in the surefire config.
	// ------------------------------------------------------------------

	@Test
	@Tag("fast")
	void taggedFast() {
		assertEquals(2, math.calculateSum(new int[] {1, 1}));
	}

	// Small helper for the @CsvSource test - turns "1,2,3" into {1,2,3}.
	private int[] toIntArray(String csv) {
		String[] parts = csv.split(",");
		int[] numbers = new int[parts.length];
		for (int i = 0; i < parts.length; i++) {
			numbers[i] = Integer.parseInt(parts[i].trim());
		}
		return numbers;
	}

}
