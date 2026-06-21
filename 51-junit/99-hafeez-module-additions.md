# Hafeez Module Additions (51-junit)

**Module:** `master-spring-and-spring-boot/51-junit`

The goal here was to enhance the existing JUnit module - **without touching the
production code**. So `MyMath.java` is exactly as the lecture left it (no new
methods, no `divide()`, no `isEven()`). Everything below is extra test cases,
annotations and modern JUnit features built on top of the one method that was
already there: `calculateSum(int[])`.

JUnit: **Jupiter 6.0.3**. Java: **17+** (required by JUnit 6).

---

## 1. What changed, file by file

### New files

| File | Why |
|---|---|
| `pom.xml` | The module had no build file, so the tests could only run inside Eclipse. This pom points at the existing `src/` and `test/` folders (nothing moved), pulls JUnit 6 in via the official BOM, and adds Surefire. `mvn test` works now. |
| `test/.../ModernJUnit5FeaturesTest.java` | A short, friendly tour of the Jupiter features the "5 steps" lecture skips: `@DisplayName`, `@ParameterizedTest` (`@ValueSource`, `@CsvSource`), `assertThrows`, `assertTimeout`, `assumeTrue`, `@RepeatedTest`, `@Disabled`, `@Nested`, `@Tag`. Every test runs against the existing `calculateSum` - one feature per test. |
| `99-hafeez-module-additions.md` | This file. |

### Modified files (tests only)

| File | What changed | Why |
|---|---|---|
| `test/.../MyMathTest.java` | Kept the original two tests untouched. Added three: `calculateSum_grouped` (`assertAll`), `calculateSum_null_throws` (`assertThrows`), plus a `@DisplayName`. | Shows the original sum tests right next to patterns you hit in real projects. `calculateSum(null)` already throws `NullPointerException`, so we get an error path to test for free. |
| `test/.../MyAssertTest.java` | Replaced the JUnit 4 import (`org.junit.Assert.assertArrayEquals`) with the Jupiter one. Turned the commented-out `assertNull` / `assertNotNull` lines into real assertions. Flipped the deliberately-failing `assertArrayEquals({1,2}, {2,1})` to a passing one, with a comment explaining the original was a red-bar demo. | The JUnit 4 import was a real latent bug - it only compiled because JUnit 4 sat on the classpath, and would have broken the build under JUnit 6. |
| `test/.../MyBeforeAfterTest.java` | Added a class-level `@DisplayName`. Methods untouched. | Keeps the lifecycle demo as-is while showing one more annotation. |
| `readme.md` | Kept all the original "Step 1-5" text and listings. Appended a **Useful References** section of official doc links at the end. | The one thing the module was missing - somewhere to go next. |

### Untouched on purpose

| File | Note |
|---|---|
| `src/.../MyMath.java` | Left exactly as the lecture shipped it. The brief was "no separate methods" - so all the new tests work against the existing `calculateSum`. |

---

## 2. Before / after

| | Before | After |
|---|---|---|
| Tests | 6 | 27 (1 intentionally skipped) |
| Production code | `MyMath.calculateSum` | `MyMath.calculateSum` (unchanged) |
| Jupiter features shown | `@Test`, lifecycle, basic asserts | + `@DisplayName`, `@ParameterizedTest`, `@ValueSource`, `@CsvSource`, `assertThrows`, `assertAll`, `assertTimeout`, `assumeTrue`, `@RepeatedTest`, `@Disabled`, `@Nested`, `@Tag` |
| Build file | none (Eclipse only) | `pom.xml` - `mvn test` works |
| JUnit version | mixed JUnit 4 import + Jupiter | Jupiter only, pinned to 6.0.3 via the BOM |
| `mvn test` | not possible (no pom) | BUILD SUCCESS, 27 tests, 1 skipped, 0 failures |

---

## 3. How to verify

```bash
cd master-spring-and-spring-boot/51-junit
mvn test
```

Expected: `Tests run: 27, Failures: 0, Errors: 0, Skipped: 1` then `BUILD SUCCESS`.
(The one skipped test is the `@Disabled` demo in `ModernJUnit5FeaturesTest`.)

---
