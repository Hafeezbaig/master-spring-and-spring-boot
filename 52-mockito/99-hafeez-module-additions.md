# 52-mockito - Module Additions (Steps 06 to 12)

Recording guide for the 7 steps added on top of the original 5-step Mockito module.

Verified locally, all green: `mvn test` -> **25 tests, 0 failures, 0 warnings on the console**.

| | |
|---|---|
| Spring Boot | 4.1.0 |
| Java | compiled for 25, run on JDK 26 |
| Mockito | 5.23.0 (transitive, via `spring-boot-starter-test`) |
| JUnit Jupiter | 6.0.3 |
| AssertJ / Hamcrest | 3.27.7 / 3.0 |

---

## Why these steps

The module stopped at "how do I make a mock return a value". Three gaps:

1. **No behavior verification.** `verify()`, captors and spies were missing entirely, so a `void` method could not be tested at all.
2. **The defaults have moved on.** `MockitoExtension` defaults to strict stubs, and Mockito 5 makes the inline mock maker standard, so `mockStatic()` works out of the box without PowerMock.
3. **Spring Boot 4 removed `@MockBean`.** Anyone following the old material gets a compile error.

Steps 06 to 09 close gap 1. Steps 10 to 12 close gaps 2 and 3.

---

## Feature list

### Already covered (Steps 00 to 05, unchanged)

- Hand-written stubs and why they do not scale
- `mock()`, `when().thenReturn()`
- `@Mock`, `@InjectMocks`, `@ExtendWith(MockitoExtension.class)`
- Chained returns, exact vs `anyInt()` argument matchers, default return values

### New (Steps 06 to 12)

| Step | Concept | File |
|---|---|---|
| 06 | `verify()`, `times()`, `never()`, `atLeastOnce()`, `verifyNoMoreInteractions()` | `business/SomeBusinessImplVerifyTest.java` |
| 07 | BDD style: `given().willReturn()`, `then().should()`, AssertJ `assertThat` | `business/SomeBusinessImplBddTest.java` |
| 08 | `ArgumentCaptor`, `@Captor`, `getValue()`, `getAllValues()` | `business/SomeBusinessImplCaptorTest.java` |
| 09 | `spy()` vs `mock()`, stubbing a spy, `doReturn().when()` | `list/SpyTest.java` |
| 10 | Strict stubs, `UnnecessaryStubbingException`, `lenient()` | `business/SomeBusinessImplStrictStubsTest.java` |
| 11 | `mockStatic()`, `MockedStatic`, no PowerMock | `staticmock/MockStaticTest.java` |
| 12 | `@MockitoBean` (Boot 4 replacement for `@MockBean`) | `business/SomeBusinessImplMockitoBeanTest.java` |

---

## Dependencies

**No new dependencies.** `spring-boot-starter-test` already brings `mockito-core`, `mockito-junit-jupiter`, AssertJ and Hamcrest. `mockStatic()` needs no `mockito-inline` on Mockito 5 - the inline mock maker is the default.

**Two `pom.xml` plugins, for a clean console.** Before this, every test run printed:

```
Mockito is currently self-attaching to enable the inline-mock-maker. This will no longer work in future releases of the JDK. Please add Mockito as an agent to your build as described in Mockito's documentation: https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html#0.3
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
WARNING: A Java agent has been loaded dynamically (.../net/bytebuddy/byte-buddy-agent/1.18.10/byte-buddy-agent-1.18.10.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
```

Six lines, on every run. Only the local repository path is shortened above.

A Java agent is a jar the JVM loads so it can rewrite classes as they are loaded. Mockito uses one to build mocks, and until now it attached that agent to the already-running JVM by itself.

This is [JEP 451](https://openjdk.org/jeps/451), shipped in JDK 21: attaching a Java agent to a running JVM still works, but the JVM warns, and it "will be disallowed by default in a future release". The JEP asks library maintainers to load the agent at startup with `-javaagent` instead, which is what this does:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>properties</goal>
            </goals>
        </execution>
    </executions>
</plugin>

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>-javaagent:"${org.mockito:mockito-core:jar}" -Xshare:off</argLine>
    </configuration>
</plugin>
```

`dependency:properties` sets one property per dependency holding the resolved path of its jar, so `${org.mockito:mockito-core:jar}` is the exact `mockito-core` Spring Boot put on the test classpath. The agent and the classpath can never drift apart, and nothing here assumes where the local repository lives or how it is laid out.

The quotes matter. Surefire splits `argLine` on spaces, so an unquoted path breaks for anyone whose local repository sits under a directory with a space in it - `C:\Users\John Doe\.m2` is the common case. Without them the JVM fails with `Error opening zip file or JAR manifest missing`, and the build never reaches a single test.

`-Xshare:off` silences the class-data-sharing warning that loading a Java agent triggers. Verified: all six lines are gone.

---

## Changes to existing files

Three changes, each one needed by a later step.

**1. `DataService` grew one method** (`main/.../business/SomeBusinessImpl.java`). Steps 06 and 08 need a call that takes an argument, so there is something to verify and capture.

```java
	//Step 06 - a void call to verify, and an argument to capture
	public void storeTheGreatestFromAllData() {
		dataService.storeGreatest(findTheGreatestFromAllData());
	}

}

interface DataService {
	int[] retrieveAllData();

	void storeGreatest(int greatestValue);

}
```

**2. Both stubs had to grow with it** (`test/.../business/SomeBusinessImplStubTest.java`). This helps **Step 02**: the step currently *claims* stubs are a maintenance burden, and now it can demonstrate that on screen.

```java
	//DataService grew one method, so every stub has to implement it.
	@Override
	public void storeGreatest(int greatestValue) {
	}
```

> Talking point for Step 02: "I added one method to the interface, and every stub class went red. A mock needs no such change."

**3. `ListTest` uses generics now** (`test/.../list/ListTest.java`). `List listMock = mock(List.class)` was raw, so Eclipse showed yellow warnings on screen. Mockito infers the type from the variable, so the class literal is not needed:

```java
		List<String> listMock = mock();
```

Also made the class package-private (`class ListTest`) to match every other test in the module.

---

# Step by step

## Step 06 - Verifying calls on Mocks

`src/test/java/com/in28minutes/mockito/mockitodemo/business/SomeBusinessImplVerifyTest.java`

```java
package com.in28minutes.mockito.mockitodemo.business;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

//Stubbing controls what the mock returns.
//Verifying checks whether the mock was called, with what, and how often.
@ExtendWith(MockitoExtension.class)
class SomeBusinessImplVerifyTest {

	@Mock
	private DataService dataServiceMock;

	@InjectMocks
	private SomeBusinessImpl businessImpl;

	@Test
	void findTheGreatestFromAllData_verifyTheCall() {
		when(dataServiceMock.retrieveAllData()).thenReturn(new int[]{25, 15, 5});

		businessImpl.findTheGreatestFromAllData();

		verify(dataServiceMock).retrieveAllData();  //no count => exactly once
		verify(dataServiceMock, times(1)).retrieveAllData();
		verify(dataServiceMock, atLeastOnce()).retrieveAllData();

		//nothing was stored, so never() passes
		verify(dataServiceMock, never()).storeGreatest(anyInt());
	}

	@Test
	void storeTheGreatestFromAllData_verifyTheArgument() {
		when(dataServiceMock.retrieveAllData()).thenReturn(new int[]{25, 15, 5});

		businessImpl.storeTheGreatestFromAllData();

		verify(dataServiceMock).storeGreatest(25);  //25 is the greatest of the three
		verify(dataServiceMock, never()).storeGreatest(15);
	}

	@Test
	void storeTheGreatestFromAllData_verifyNothingElseHappened() {
		when(dataServiceMock.retrieveAllData()).thenReturn(new int[]{25, 15, 5});

		businessImpl.storeTheGreatestFromAllData();

		verify(dataServiceMock).retrieveAllData();
		verify(dataServiceMock).storeGreatest(25);
		verifyNoMoreInteractions(dataServiceMock);  //fails if we missed a call
	}

}
```

- Until now every test asked "what came back". This step asks "was it even called".
- Needed whenever the method returns `void` - there is no return value to assert on.
- Live demo: change `verify(dataServiceMock).storeGreatest(25)` to `storeGreatest(15)` and read the failure message. Mockito prints the actual argument it saw.

Docs: [Mockito javadoc](https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html)

---

## Step 07 - BDD Style - given, willReturn, then, should

`src/test/java/com/in28minutes/mockito/mockitodemo/business/SomeBusinessImplBddTest.java`

```java
package com.in28minutes.mockito.mockitodemo.business;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

//The same tests in given/when/then form.
//when-thenReturn becomes given-willReturn, and verify becomes then-should.
@ExtendWith(MockitoExtension.class)
class SomeBusinessImplBddTest {

	@Mock
	private DataService dataServiceMock;

	@InjectMocks
	private SomeBusinessImpl businessImpl;

	@Test
	void findTheGreatestFromAllData_basicScenario() {
		//given
		given(dataServiceMock.retrieveAllData()).willReturn(new int[]{25, 15, 5});

		//when
		int result = businessImpl.findTheGreatestFromAllData();

		//then
		assertThat(result).isEqualTo(25);
		then(dataServiceMock).should().retrieveAllData();
	}

	@Test
	void storeTheGreatestFromAllData_basicScenario() {
		//given
		given(dataServiceMock.retrieveAllData()).willReturn(new int[]{25, 15, 5});

		//when
		businessImpl.storeTheGreatestFromAllData();

		//then
		then(dataServiceMock).should().storeGreatest(25);
		then(dataServiceMock).should(never()).storeGreatest(15);
	}

}
```

- Nothing new is being tested. Only the vocabulary changes, and the `//given //when //then` comments mark the three parts of every test.
- `when` is an overloaded word in Mockito: it means "stub this" but reads as "the action". BDD removes the clash.
- This is also where AssertJ's `assertThat(result).isEqualTo(25)` comes in. It ships inside `spring-boot-starter-test`, so there is nothing to add.
- Worth saying out loud: the module readme already asks you to add `org.mockito.BDDMockito` to Eclipse Favorites. This is the step that uses it.

Docs: [BDDMockito](https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/BDDMockito.html) | [AssertJ](https://assertj.github.io/doc/)

---

## Step 08 - Capturing arguments with ArgumentCaptor

`src/test/java/com/in28minutes/mockito/mockitodemo/business/SomeBusinessImplCaptorTest.java`

```java
package com.in28minutes.mockito.mockitodemo.business;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

//verify(mock).storeGreatest(25) only works when we already know the value.
//A captor records what the code actually passed, for arguments built inside the method.
@ExtendWith(MockitoExtension.class)
class SomeBusinessImplCaptorTest {

	@Mock
	private DataService dataServiceMock;

	@InjectMocks
	private SomeBusinessImpl businessImpl;

	@Captor
	private ArgumentCaptor<Integer> greatestValueCaptor;

	@Test
	void storeTheGreatestFromAllData_captureTheArgument() {
		when(dataServiceMock.retrieveAllData()).thenReturn(new int[]{25, 15, 5});

		businessImpl.storeTheGreatestFromAllData();

		verify(dataServiceMock).storeGreatest(greatestValueCaptor.capture());
		assertThat(greatestValueCaptor.getValue()).isEqualTo(25);
	}

	@Test
	void storeTheGreatestFromAllData_captureMultipleCalls() {
		when(dataServiceMock.retrieveAllData())
				.thenReturn(new int[]{25, 15, 5})
				.thenReturn(new int[]{35});

		businessImpl.storeTheGreatestFromAllData();
		businessImpl.storeTheGreatestFromAllData();

		verify(dataServiceMock, times(2)).storeGreatest(greatestValueCaptor.capture());
		assertThat(greatestValueCaptor.getAllValues()).containsExactly(25, 35);
	}

}
```

- Order matters: `capture()` goes **inside** `verify()`, and you read the value **after**.
- `getValue()` is the last call. `getAllValues()` is every call, in order - that is the second test.
- The chained `thenReturn().thenReturn()` revisits Step 05.
- `@Captor` saves you writing `ArgumentCaptor.forClass(Integer.class)`.

Docs: [ArgumentCaptor](https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/ArgumentCaptor.html)

---

## Step 09 - Introduction to Spy

`src/test/java/com/in28minutes/mockito/mockitodemo/list/SpyTest.java`

```java
package com.in28minutes.mockito.mockitodemo.list;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

//A mock does nothing unless you stub it.
//A spy wraps a real object: the real behavior runs, and you can still stub and verify it.
//Prefer a mock. A spy is for code you cannot redesign.
class SpyTest {

	@Test
	void mock_ignoresTheRealBehavior() {
		//mock() infers the type from the variable, so List.class is not needed
		List<String> listMock = mock();

		listMock.add("SomeString");

		assertThat(listMock.size()).isZero();     //the add() went nowhere
		assertThat(listMock.get(0)).isNull();
	}

	@Test
	void spy_keepsTheRealBehavior() {
		List<String> listSpy = spy(new ArrayList<String>());

		listSpy.add("SomeString");

		assertThat(listSpy.size()).isEqualTo(1);  //a real ArrayList did the work
		assertThat(listSpy.get(0)).isEqualTo("SomeString");
		verify(listSpy).add("SomeString");        //and it is still a Mockito mock
	}

	@Test
	void spy_canStillBeStubbed() {
		List<String> listSpy = spy(new ArrayList<String>());
		listSpy.add("SomeString");

		when(listSpy.size()).thenReturn(10);      //stubbing wins over real behavior

		//everything the stub does not cover stays real
		assertThat(listSpy.size()).isEqualTo(10);
		assertThat(listSpy.get(0)).isEqualTo("SomeString");
	}

	//Watch the line above: when(listSpy.size()) runs the real size() before it stubs anything.
	//On an ArrayList that is harmless. On a method that throws, or writes to a database, it is not.
	//doReturn().when() never calls the real method, so it is the safe form for a spy.
	@Test
	void spy_stubbedWithoutCallingTheRealMethod() {
		List<String> listSpy = spy(new ArrayList<String>());

		doReturn(10).when(listSpy).size();

		assertThat(listSpy.size()).isEqualTo(10);
	}

}
```

- Runs the same setup against a mock and a spy back to back. `size()` is `0` on the mock and `1` on the spy. That contrast is the whole step.
- Third test is the surprise: you can stub a spy, and the stub takes precedence over the real method. Everything you do not stub stays real.
- **Fourth test is the trap.** `when(spy.size())` has to call the real `size()` to reach the stubbing. On an `ArrayList` nobody notices. On a method that throws, or hits a database, the test blows up during setup and the message points nowhere useful. `doReturn().when()` skips the real call.
- Say the caveat: a spy means you are testing real code you probably meant to isolate. Prefer a mock. `@Spy` exists as the annotation form.

Docs: [Mockito javadoc](https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html)

---

## Step 10 - Strict Stubs and UnnecessaryStubbingException

`src/test/java/com/in28minutes/mockito/mockitodemo/business/SomeBusinessImplStrictStubsTest.java`

```java
package com.in28minutes.mockito.mockitodemo.business;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

//MockitoExtension defaults to strict stubs. Two failures it reports:
// - UnnecessaryStubbingException : you stubbed something the code never called
// - PotentialStubbingProblem     : the code called your stub with a different argument
//lenient() opts a single stub out.
@ExtendWith(MockitoExtension.class)
class SomeBusinessImplStrictStubsTest {

	@Mock
	private DataService dataServiceMock;

	@InjectMocks
	private SomeBusinessImpl businessImpl;

	@Test
	void stubThatIsUsed_isHappy() {
		when(dataServiceMock.retrieveAllData()).thenReturn(new int[]{25, 15, 5});

		assertThat(businessImpl.findTheGreatestFromAllData()).isEqualTo(25);
	}

	//DEMO: delete lenient() below and run again -> UnnecessaryStubbingException
	@Test
	void stubThatIsNeverUsed_needsLenient() {
		lenient().when(dataServiceMock.retrieveAllData()).thenReturn(new int[]{25, 15, 5});

		assertThat(businessImpl).isNotNull();  //we never call retrieveAllData()
	}

}
```

- **The live demo is the step.** Delete `lenient()`, run, get the red bar. Verified output, in full:

```
org.mockito.exceptions.misusing.UnnecessaryStubbingException:

Unnecessary stubbings detected.
Clean & maintainable test code requires zero unnecessary code.
Following stubbings are unnecessary (click to navigate to relevant line of code):
  1. -> at com.in28minutes.mockito.mockitodemo.business.SomeBusinessImplStrictStubsTest.stubThatIsNeverUsed_needsLenient(SomeBusinessImplStrictStubsTest.java:36)
Please remove unnecessary stubbings or use 'lenient' strictness.
```

Point at the clickable line number while it is on screen - that is the part people miss.

- Put it back, green again. Then say why: an unused stub is setup that no longer matches the code, and strictness reports it instead of leaving it in place.
- Worth two minutes, because it is a common surprise for anyone following older Mockito material.
- Mention the sibling error `PotentialStubbingProblem`: you stubbed `get(0)` but the code called `get(1)`. Under lenient strictness that call quietly returns `null`; under strict stubs it fails.

Docs: [UnnecessaryStubbingException](https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/exceptions/misusing/UnnecessaryStubbingException.html) | [Strictness](https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/quality/Strictness.html)

---

## Step 11 - Mocking static methods, without PowerMock

`src/test/java/com/in28minutes/mockito/mockitodemo/staticmock/MockStaticTest.java`

```java
package com.in28minutes.mockito.mockitodemo.staticmock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

//Static methods once required PowerMock. Mockito 5 makes the inline mock maker the default,
//so mockStatic() works with no extra dependency, as does mocking final classes.
//The mock is scoped to this thread and this try block, so it has to be closed.
class MockStaticTest {

	@Test
	void mockStatic_freezeTheClock() {
		//a real LocalDate, built before the static mock exists
		LocalDate fixedDate = LocalDate.of(2000, 1, 1);

		try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
			mockedLocalDate.when(LocalDate::now).thenReturn(fixedDate);

			assertThat(LocalDate.now()).isEqualTo(fixedDate);
			mockedLocalDate.verify(LocalDate::now);   //statics can be verified as well
		}

		//outside the try block the mock is closed, so the real behavior is back
		assertThat(LocalDate.now()).isNotEqualTo(fixedDate);
	}

}
```

- Freezing the clock is the clearest real-world case. Date-dependent code was awkward to test before this.
- `try`-with-resources is not decoration. The static mock is global to the thread until closed, so leaking it breaks unrelated tests.
- The last line, outside the block, proves the scoping. Worth pointing out on screen.
- The headline: **`mockito-inline` and PowerMock are no longer needed.** Anyone who learned static mocking before Mockito 5 reaches for PowerMock out of habit; this is where to say it is no longer necessary.
- The same mechanism handles final classes and final methods, which the subclass mock maker cannot.

Docs: [MockedStatic](https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/MockedStatic.html)

---

## Step 12 - Mocking a Spring Bean with @MockitoBean

`src/test/java/com/in28minutes/mockito/mockitodemo/business/SomeBusinessImplMockitoBeanTest.java`

```java
package com.in28minutes.mockito.mockitodemo.business;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

//@Mock creates a mock for a plain JUnit test. @MockitoBean puts one in the Spring context,
//so injected beans receive it. Spring Boot 4 removed @MockBean and @SpyBean - use
//@MockitoBean and @MockitoSpyBean, imported from spring-test.
@SpringBootTest
class SomeBusinessImplMockitoBeanTest {

	@MockitoBean
	private DataService dataServiceMock;

	@Autowired
	private SomeBusinessImpl businessImpl;

	@Test
	void findTheGreatestFromAllData_withTheMockInsideTheSpringContext() {
		when(dataServiceMock.retrieveAllData()).thenReturn(new int[]{25, 15, 5});

		assertThat(businessImpl.findTheGreatestFromAllData()).isEqualTo(25);
	}

	//SomeBusinessImpl is not a @Service in this module, so register it just for this test
	@TestConfiguration
	static class TestConfig {

		@Bean
		SomeBusinessImpl someBusinessImpl(DataService dataService) {
			return new SomeBusinessImpl(dataService);
		}

	}

}
```

- Every step so far ran without Spring. This one starts a Spring context, so it is the bridge to the Spring Boot testing material.
- `@Mock` vs `@MockitoBean` in one line: same Mockito mock, but `@MockitoBean` registers it in the Spring container so `@Autowired` beans receive it.
- **Flag the breaking change clearly.** `@MockBean` and `@SpyBean` are gone in Spring Boot 4, not just deprecated - the classes are no longer in `spring-boot-test`. Deprecated since Boot 3.4, removed in 4.
- Watch the import. `org.springframework.test.context.bean.override.mockito.MockitoBean` - it moved to `spring-test`.
- Not a 1-to-1 swap: `@MockitoBean` uses replace-or-create, `@MockitoSpyBean` wraps the existing bean.
- The `@TestConfiguration` is only here because `SomeBusinessImpl` is a plain class in this module, not a `@Service`. Say so out loud, so nobody copies the nested config into a real project where the bean already exists.

Docs: [@MockitoBean and @MockitoSpyBean](https://docs.spring.io/spring-framework/reference/testing/annotations/integration-spring/annotation-mockitobean.html) | [Boot 4 migration guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)

---

## Not included, and why

- **Hamcrest matchers.** AssertJ ships in the same starter and reads better. Step 07 uses AssertJ instead.
- **PowerMock.** No longer needed for static or final mocking. Step 11 covers that with plain Mockito.
- **`mockConstruction()`.** Same mechanism as Step 11, narrow use. Mention it in passing.
- **`@MockitoSpyBean` demo.** `@MockitoSpyBean` wraps an existing bean, and this module has no real bean worth wrapping - `DataService` has no implementation outside the tests. Named in Step 12, alongside `@MockitoBean`, without a demo.

## Running it

```
cd 52-mockito
mvn test
```

Expect 25 tests and a console with no warnings. If you see "Mockito is currently self-attaching", the two plugins in `pom.xml` are missing.
