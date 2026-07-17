package com.in28minutes.junit;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Reference: https://docs.junit.org/6.1.0/writing-tests/parameterized-classes-and-tests.html
@DisplayName("MyMath Tests with Parameterized Methods")
class MyMathParameterizedTest {

    private final MyMath math = new MyMath();

    @ParameterizedTest(name = "Test {index}: multiplier = {0}")
    @ValueSource(ints = {1, 2, 3, 4, 5})
    @DisplayName("Calculate sum with different multipliers")
    void calculateSum_WithDifferentMultipliers(int multiplier) {
        int baseSum = math.calculateSum(new int[]{1, 2, 3});
        int result = baseSum * multiplier;
        assertTrue(result > 0);
        System.out.println("Multiplier: " + multiplier + ", Result: " + result);
    }
}

// @RepeatedTest(for simple repetition without data variation) internally uses @TestTemplate
// Reference: https://docs.junit.org/6.1.0/writing-tests/repeated-tests.html
// https://docs.junit.org/6.1.0/api/org.junit.jupiter.api/org/junit/jupiter/api/TestTemplate.html
@DisplayName("MyMath Repeated Tests (Simpler Alternative)")
class MyMathRepeatedTemplateTest {

    private MyMath math = new MyMath();

    @RepeatedTest(4)
    @DisplayName("Repeat sum calculations")
    void repeatCalculateSum(RepetitionInfo info) {
        switch (info.getCurrentRepetition()) {
            case 1:
                assertEquals(6, math.calculateSum(new int[]{1, 2, 3}));
                break;
            case 2:
                assertEquals(0, math.calculateSum(new int[]{}));
                break;
            case 3:
                assertEquals(30, math.calculateSum(new int[]{5, 10, 15}));
                break;
            case 4:
                assertEquals(7, math.calculateSum(new int[]{7}));
                break;
        }
    }
}

// Reference: https://jspecify.dev
// https://jspecify.dev/docs/api/org/jspecify/annotations/package-summary.html
@NullMarked
class MyMathWithNullability {
    public int calculateSum(int[] numbers) {
        int sum = 0;
        for (int number : numbers) {
            sum += number;
        }
        return sum;
    }

    public void process(@Nullable String input) {
        if (input != null) {
            System.out.println(input);
        }
    }
}

class MyMathNullabilityTest {
    private final MyMathWithNullability math = new MyMathWithNullability();

    @Test
    void testNullableParameter() {
        math.process(null);
    }

    @Test
    void testNonNullRequired() {
        assertEquals(6, math.calculateSum(new int[]{1, 2, 3}));
    }
}


// Enhanced Timeout Support
// (passed)
// Reference: https://docs.junit.org/6.1.0/writing-tests/timeouts.html
class MyMathTimeoutTest {
    private final MyMath math = new MyMath();

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    @DisplayName("Test must complete within 2 seconds")
    void calculateSum_WithTimeout() {
        assertEquals(6, math.calculateSum(new int[]{1, 2, 3}));
    }

    @Test
    @Timeout(value = 100, unit = TimeUnit.MILLISECONDS)
    @DisplayName("Quick execution test")
    void calculateSum_FastExecution() {
        assertEquals(0, math.calculateSum(new int[]{}));
    }
}

// ============================================================================================================================================================================================================================

// Built-in Extensions - System Properties
// (passed)
// Reference: https://docs.junit.org/6.1.0/writing-tests/built-in-extensions.html#system-properties
class MyMathWithSystemPropertiesTest {
    private final MyMath math = new MyMath();

    @Test
    void testWithSystemProperty() {
        System.setProperty("test.mode", "junit6");
        assertEquals(6, math.calculateSum(new int[]{1, 2, 3}));
    }
}

// ============================================================================================================================================================================================================================

// Default Locale and TimeZone
// (passed)
// Reference: https://docs.junit.org/6.1.0/writing-tests/built-in-extensions.html#DefaultLocaleAndTimeZone
@DisplayName("Tests with specific Locale and TimeZone")
class MyMathLocalizationTest {

    @Test
    void testWithUSLocale() {
        Locale.setDefault(Locale.US);
        assertEquals(6, new MyMath().calculateSum(new int[]{1, 2, 3}));
    }

    @Test
    void testWithIndianTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("IST"));
        assertEquals(6, new MyMath().calculateSum(new int[]{1, 2, 3}));
    }
}

// ============================================================================================================================================================================================================================

// Deterministic @Nested Class Ordering
// (passed)
// Reference: https://docs.junit.org/6.1.0/writing-tests/nested-tests.html

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("MyMath Tests with Nested Class Ordering")
class MyMathNestedOrderingTest {
    private MyMath math = new MyMath();

    @Nested
    @DisplayName("A: First Nested Class (alphabetically)")
    class FirstNestedTest {
        @Test
        void test() {
            assertEquals(6, math.calculateSum(new int[]{1, 2, 3}));
        }
    }

    @Nested
    @DisplayName("B: Second Nested Class (alphabetically)")
    class SecondNestedTest {
        @Test
        void test() {
            assertEquals(0, math.calculateSum(new int[]{}));
        }
    }
}