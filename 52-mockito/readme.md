# First 5 Steps in Mockito

Mockito is the most famous mocking framework in Java.

## Installing Tools

### Our Recommendations

- Use **latest version** of Java
- Use **latest version** of "Eclipse IDE for Enterprise Java Developers"
- Remember: Spring Boot 3+ works only with Java 17+

### Installing Java

- Windows - https://www.youtube.com/watch?v=I0SBRWVS0ok
- Linux - https://www.youtube.com/watch?v=mHvFpyHK97A
- Mac - https://www.youtube.com/watch?v=U3kTdMPlgsY

#### Troubleshooting

- Troubleshooting Java Installation - https://www.youtube.com/watch?v=UI_PabQ1YB0

### Installing Eclipse

- Windows - https://www.youtube.com/watch?v=toY06tsME-M
- Others - https://www.youtube.com/watch?v=XveQ9Gq41UM

#### Troubleshooting
- Configuring Java in Eclipse - https://www.youtube.com/watch?v=8i0r_fcE3L0

## Easier Static Imports
- Window > Preferences > Java > Editor > Content Assist > Favorites
- org.junit.Assert
- org.mockito.BDDMockito
- org.mockito.Mockito
- org.hamcrest.Matchers
- org.hamcrest.CoreMatchers
- More information 
- Visit Mockito Official Documentation - [Mockito Documentation] (http://site.mockito.org/mockito/docs/current/org/mockito/Mockito.html)

## Step by Step Details

- Step 00 - Introduction to Section - Mockito in 5 Steps
- Step 01 - Setting up a Spring Boot Project
- Step 02 - Understanding problems with Stubs
- Step 03 - Writing your first Mockito test with Mocks
- Step 04 - Simplifying Tests with Mockito Annotations - @Mock, @InjectMocks
- Step 05 - Exploring Mocks further by Mocking List interface
<!---
Current Directory : /Users/rangakaranam/Ranga/git/00.courses/spring-boot-master-class/04.Mockito-Introduction-In-5-Steps-V2
-->

## Complete Code Example


### /pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>4.0.6</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.in28minutes.mockito</groupId>
	<artifactId>mockito-demo</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>mockito-demo</name>
	<description>Demo project for Spring Boot</description>
	<properties>
		<java.version>25</java.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>
	

</project>
```
---

### /src/main/java/com/in28minutes/mockito/mockitodemo/MockitoDemoApplication.java

```java
package com.in28minutes.mockito.mockitodemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MockitoDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MockitoDemoApplication.class, args);
	}

}
```
---

### /src/main/java/com/in28minutes/mockito/mockitodemo/business/SomeBusinessImpl.java

```java
package com.in28minutes.mockito.mockitodemo.business;

public class SomeBusinessImpl {
	
	private DataService dataService;
	
	public SomeBusinessImpl(DataService dataService) {
		super();
		this.dataService = dataService;
	}
	
	public int findTheGreatestFromAllData() {
		int[] data = dataService.retrieveAllData();
		int greatestValue = Integer.MIN_VALUE;
		for(int value:data) {
			if(value > greatestValue)
				greatestValue = value;
		}
		return greatestValue;
	}

}

interface DataService {
	int[] retrieveAllData();
	
	
}
```
---

### /src/main/resources/application.properties

```properties

```
---

### /src/test/java/com/in28minutes/mockito/mockitodemo/MockitoDemoApplicationTests.java

```java
package com.in28minutes.mockito.mockitodemo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MockitoDemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
```
---

### /src/test/java/com/in28minutes/mockito/mockitodemo/business/SomeBusinessImplMockTest.java

```java
package com.in28minutes.mockito.mockitodemo.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SomeBusinessImplMockTest {
	
	@Mock
	private DataService dataServiceMock;
	
	@InjectMocks
	private SomeBusinessImpl businessImpl;

	@Test
	void findTheGreatestFromAllData_basicScenario() {
		when(dataServiceMock.retrieveAllData()).thenReturn(new int[]{25, 15, 5});
		assertEquals(25, businessImpl.findTheGreatestFromAllData());
	}
	
	@Test
	void findTheGreatestFromAllData_OneValue() {
		when(dataServiceMock.retrieveAllData()).thenReturn(new int[]{35});
		assertEquals(35, businessImpl.findTheGreatestFromAllData());
	}

	@Test
	void findTheGreatestFromAllData_EmptyArray() {
		when(dataServiceMock.retrieveAllData()).thenReturn(new int[]{});
		assertEquals(Integer.MIN_VALUE, businessImpl.findTheGreatestFromAllData());
	}

}
```
---

### /src/test/java/com/in28minutes/mockito/mockitodemo/business/SomeBusinessImplStubTest.java

```java
package com.in28minutes.mockito.mockitodemo.business;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SomeBusinessImplStubTest {

	@Test
	void findTheGreatestFromAllData_basicScenario() {
		DataService dataServiceStub = new DataServiceStub1();
		SomeBusinessImpl businessImpl = new SomeBusinessImpl(dataServiceStub);
		int result = businessImpl.findTheGreatestFromAllData();
		assertEquals(25, result);
	}

	@Test
	void findTheGreatestFromAllData_withOneValue() {
		DataService dataServiceStub = new DataServiceStub2();
		SomeBusinessImpl businessImpl = new SomeBusinessImpl(dataServiceStub);
		int result = businessImpl.findTheGreatestFromAllData();
		assertEquals(35, result);
	}

}

class DataServiceStub1 implements DataService {

	@Override
	public int[] retrieveAllData() {
		return new int[]{25, 15, 5};
	}
	
}


class DataServiceStub2 implements DataService {

	@Override
	public int[] retrieveAllData() {
		return new int[]{35};
	}
	
}
```
---

### /src/test/java/com/in28minutes/mockito/mockitodemo/list/ListTest.java

```java
package com.in28minutes.mockito.mockitodemo.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ListTest {
	
	@Test
	void simpleTest() {
		List listMock = mock(List.class);
		//listMock.size() => 3
		when(listMock.size()).thenReturn(3);	
		assertEquals(3, listMock.size());
		assertEquals(3, listMock.size());
		assertEquals(3, listMock.size());
		assertEquals(3, listMock.size());
	}

	@Test
	void multipleReturns() {
		List listMock = mock(List.class);
		//listMock.size() => 3
		when(listMock.size()).thenReturn(1).thenReturn(2);	
		assertEquals(1, listMock.size());
		assertEquals(2, listMock.size());
		assertEquals(2, listMock.size());
		assertEquals(2, listMock.size());
	}
	
	@Test
	void specificParameters() {
		List listMock = mock(List.class);
		//listMock.size() => 3
		when(listMock.get(0)).thenReturn("SomeString");	
		assertEquals("SomeString", listMock.get(0));
		assertEquals(null, listMock.get(1));
	}

	@Test
	void genericParameters() {
		List listMock = mock(List.class);
		//listMock.size() => 3
		when(listMock.get(Mockito.anyInt())).thenReturn("SomeOtherString");	
		assertEquals("SomeOtherString", listMock.get(0));
		assertEquals("SomeOtherString", listMock.get(1));
	}

}
```
---

## How to Run

Needs Java 17+ (Spring Boot 4 needs 17 or higher) and Maven 3.8+.

```bash
mvn test
```

Expected: `Tests run: 18, Failures: 0, Errors: 0, Skipped: 0` then `BUILD SUCCESS`.

That is the 10 tests from the lecture (stub, mock, list) plus 8 new ones in
`SomeBusinessImplMockitoFeaturesTest` (see below).

**Eclipse:** *File > Import > Existing Maven Projects* > pick this folder >
right-click a test > *Run As > JUnit Test*.

### About the Mockito agent line in pom.xml

On newer JDKs Mockito used to print this on every run:

> Mockito is currently self-attaching to enable the inline-mock-maker. This
> will no longer work in future releases of the JDK.

Mockito needs a Java agent to mock final classes and methods. The old way it
loaded that agent is being switched off in future JDKs. The fix is to load
Mockito as an agent when the test JVM starts. The `pom.xml` does this in the
Surefire plugin:

```xml
<argLine>-javaagent:${settings.localRepository}/org/mockito/mockito-core/${mockito.version}/mockito-core-${mockito.version}.jar</argLine>
```

`${mockito.version}` comes from the Spring Boot parent, so there is no version
to keep in sync. The warning is gone and the build is ready for newer JDKs.

---

## Notes

### What the lecture covers vs what the new test file adds

| Lecture (first 5 steps)                  | New file `SomeBusinessImplMockitoFeaturesTest` |
|------------------------------------------|------------------------------------------------|
| `mock(Class)`                            | type-inferred `mock()`                          |
| `when(...).thenReturn(...)`              | `verify(...)`, `times(n)`, `never()`            |
| multiple returns                         | `thenThrow(...)` for the error path             |
| `@Mock`, `@InjectMocks`                  | `@Captor` + `ArgumentCaptor`                    |
| `anyInt()`                               | `any(...)`, `eq(...)`                           |
| (not shown)                              | BDD style: `given().willReturn()`, `then().should()` |
| (not shown)                              | `@Spy` / `spy(...)` (real object, partial stub) |
| (not shown)                              | AssertJ `assertThat(...)` (ships with the test starter) |

All new examples run over the course's own `SomeBusinessImpl` / `DataService`,
so there is nothing new to learn about the domain.

### Stubbing cheat sheet (what a mock should return)

| Call                                        | Meaning                                      |
|---------------------------------------------|----------------------------------------------|
| `when(m.x()).thenReturn(v)`                 | Return `v` when `x()` is called              |
| `when(m.x()).thenReturn(a).thenReturn(b)`   | Return `a` first call, `b` after that        |
| `when(m.x()).thenThrow(new E())`            | Throw instead of returning                   |
| `given(m.x()).willReturn(v)`                | Same as `thenReturn`, BDD wording            |
| `doReturn(v).when(spy).x()`                 | Stub a spy without calling the real `x()`    |

### Verification cheat sheet (how a mock was used)

| Call                                  | Checks that...                          |
|---------------------------------------|-----------------------------------------|
| `verify(m).x()`                       | `x()` was called exactly once           |
| `verify(m, times(2)).x()`             | `x()` was called twice                  |
| `verify(m, never()).x()`              | `x()` was never called                  |
| `verify(m, atLeastOnce()).x()`        | `x()` was called one or more times      |
| `then(m).should().x()`                | Same as `verify`, BDD wording           |
| `verifyNoInteractions(m)`             | The mock was never touched at all       |

### Argument matchers

| Matcher          | Matches                                  |
|------------------|------------------------------------------|
| `any()`          | Any value (including null on some types) |
| `any(String.class)` | Any non-null `String`                 |
| `anyInt()`, `anyString()` | Any value of that type          |
| `eq(value)`      | Exactly `value`                          |

Rule: if you use a matcher for one argument of a call, use matchers for every
argument of that call (e.g. `m.foo(eq("a"), any())`, not `m.foo("a", any())`).

### Annotations (need `@ExtendWith(MockitoExtension.class)`)

| Annotation     | Does                                                |
|----------------|-----------------------------------------------------|
| `@Mock`        | Creates a mock for that field                       |
| `@InjectMocks` | Builds the real object and injects the mocks into it|
| `@Spy`         | Wraps a real object; un-stubbed methods run for real|
| `@Captor`      | Creates an `ArgumentCaptor` for that field          |

### mock vs spy

| | `mock(T.class)` | `spy(realObject)` |
|---|---|---|
| Backing object | none, all methods faked | a real instance |
| Un-stubbed method | returns default (0, null, empty) | runs the real method |
| Use when | you do not need real behaviour | you want real behaviour but to override a bit |

### Style tips
- Name tests like sentences: `findGreatest_emptyArray_returnsMinValue()`.
- One behaviour per test.
- Stub only what the test needs. `MockitoExtension` is strict and will flag
  unused stubs, which keeps tests honest.
- Use `verify` to check side effects (a call happened), not to repeat what the
  assertion already proved.

> `SomeBusinessImplMockitoFeaturesTest.java` has runnable examples of every row
> in the tables above.

---

## Useful References

### Mockito (official)
- Mockito site : https://site.mockito.org/
- `Mockito` Javadoc (the big how-to) : https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html
- `BDDMockito` Javadoc : https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/BDDMockito.html
- `ArgumentCaptor` Javadoc : https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/ArgumentCaptor.html
- `ArgumentMatchers` Javadoc : https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/ArgumentMatchers.html
- `MockitoExtension` (JUnit 5) : https://javadoc.io/doc/org.mockito/mockito-junit-jupiter/latest/org.mockito.junit.jupiter/org/mockito/junit/jupiter/MockitoExtension.html
- Mockito on GitHub : https://github.com/mockito/mockito
- Release notes : https://github.com/mockito/mockito/releases
- Mockito wiki (FAQ, how to write good tests) : https://github.com/mockito/mockito/wiki

### The Java agent change (the warning this module fixes)
- Mockito + newer JDK agent setup : https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html#0.3
- Configure the Mockito agent for Java 21+ (rieckpil) : https://rieckpil.de/how-to-configure-mockito-agent-for-java-21-without-warning/
- JEP 451 (why dynamic agent loading is being restricted) : https://openjdk.org/jeps/451

### JUnit 5 (the test runner used here)
- JUnit 5 User Guide : https://docs.junit.org/current/user-guide/
- `@ExtendWith` and extensions : https://docs.junit.org/current/user-guide/#extensions

### Spring Boot testing
- Testing in Spring Boot : https://docs.spring.io/spring-boot/reference/testing/index.html
- `spring-boot-starter-test` (what it bundles) : https://docs.spring.io/spring-boot/reference/testing/test-scope-dependencies.html
- Spring Boot Maven plugin : https://docs.spring.io/spring-boot/maven-plugin/index.html

### Going further
- AssertJ (fluent assertions, bundled with the starter) : https://assertj.github.io/doc/
- Hamcrest matchers : http://hamcrest.org/JavaHamcrest/
- Maven Surefire plugin : https://maven.apache.org/surefire/maven-surefire-plugin/
- Martin Fowler, Mocks Aren't Stubs (mock vs stub) : https://martinfowler.com/articles/mocksArentStubs.html
