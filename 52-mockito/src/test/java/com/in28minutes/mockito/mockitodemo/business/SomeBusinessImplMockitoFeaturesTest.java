package com.in28minutes.mockito.mockitodemo.business;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Mockito features the 5-step lecture does not cover, written over the
 * course's own SomeBusinessImpl / DataService so the examples stay familiar.
 *
 * The lecture shows: mock(), when().thenReturn(), @Mock, @InjectMocks, anyInt().
 * This file adds the things you reach for on a real project:
 *   verify()         - check a method was (or was not) called
 *   ArgumentCaptor   - capture what was passed to a mock, then assert on it
 *   BDDMockito       - given/willReturn + then/should (given-when-then naming)
 *   thenThrow        - make a mock blow up, to test error paths
 *   @Spy             - wrap a REAL object, stub only the parts you want
 *   matchers         - any(), eq() for flexible argument matching
 *
 * One short example per feature.
 * Docs: https://site.mockito.org and https://javadoc.io/doc/org.mockito/mockito-core/latest/
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Mockito features beyond the first 5 steps")
class SomeBusinessImplMockitoFeaturesTest {

	@Mock
	private DataService dataServiceMock;

	// Mockito builds SomeBusinessImpl and passes the mock into its constructor.
	@InjectMocks
	private SomeBusinessImpl businessImpl;

	// ------------------------------------------------------------------
	// verify(): assert HOW a mock was used, not just what it returned.
	// Here: findTheGreatestFromAllData must read the data source exactly once.
	// ------------------------------------------------------------------

	@Test
	void verify_dataServiceIsCalledExactlyOnce() {
		when(dataServiceMock.retrieveAllData()).thenReturn(new int[] {25, 15, 5});

		businessImpl.findTheGreatestFromAllData();

		verify(dataServiceMock).retrieveAllData();          // default is times(1)
		verify(dataServiceMock, times(1)).retrieveAllData();
	}

	// never(): prove a method was NOT called. Here we never run the business
	// method, so the data source must stay untouched.
	@Test
	void verify_methodIsNeverCalledWhenBusinessIsNotRun() {
		verify(dataServiceMock, never()).retrieveAllData();
	}

	// ------------------------------------------------------------------
	// thenThrow: test the unhappy path. Make the dependency fail and check
	// the exception bubbles up.
	// ------------------------------------------------------------------

	@Test
	void thenThrow_propagatesExceptionFromDependency() {
		when(dataServiceMock.retrieveAllData())
				.thenThrow(new RuntimeException("data source is down"));

		RuntimeException ex = assertThrows(RuntimeException.class,
				() -> businessImpl.findTheGreatestFromAllData());
		assertEquals("data source is down", ex.getMessage());
	}

	// ------------------------------------------------------------------
	// BDDMockito: same behaviour, friendlier names that read as
	// given / when / then. given(...).willReturn(...) instead of when().thenReturn(),
	// then(mock).should() instead of verify(mock).
	// ------------------------------------------------------------------

	@Test
	void bddStyle_givenWillReturn_thenShould() {
		// given
		given(dataServiceMock.retrieveAllData()).willReturn(new int[] {25, 15, 5});

		// when
		int result = businessImpl.findTheGreatestFromAllData();

		// then
		assertEquals(25, result);
		then(dataServiceMock).should().retrieveAllData();
	}

	// ------------------------------------------------------------------
	// ArgumentCaptor (+ @Captor): grab the exact value passed to a mock so you
	// can assert on it. Shown on a mocked List, like the lecture's ListTest.
	// ------------------------------------------------------------------

	@Captor
	private ArgumentCaptor<String> stringCaptor;

	@Test
	void argumentCaptor_capturesWhatWasPassedToTheMock() {
		List<String> listMock = mock();

		listMock.add("first call");

		verify(listMock).add(stringCaptor.capture());
		assertEquals("first call", stringCaptor.getValue());
	}

	// ------------------------------------------------------------------
	// @Spy / spy(): wrap a REAL object. Un-stubbed methods run for real,
	// so the list below actually stores items; you stub only what you need.
	// ------------------------------------------------------------------

	@Test
	void spy_realMethodsRunUnlessStubbed() {
		List<String> listSpy = spy(new ArrayList<>());

		listSpy.add("real add");                 // real ArrayList behaviour
		assertEquals(1, listSpy.size());          // really stored

		when(listSpy.size()).thenReturn(99);      // override just size()
		assertEquals(99, listSpy.size());
	}

	// ------------------------------------------------------------------
	// Argument matchers: any() / eq() let one stub answer many inputs.
	// Rule: if you use a matcher for one argument, use matchers for all of them.
	// ------------------------------------------------------------------

	@Test
	void matchers_anyAndEq() {
		List<String> listMock = mock();
		when(listMock.get(anyInt())).thenReturn("same for any index");

		assertEquals("same for any index", listMock.get(0));
		assertEquals("same for any index", listMock.get(42));

		listMock.add("hello");
		verify(listMock).add(any(String.class)); // matches whatever was added
	}

	// ------------------------------------------------------------------
	// Bonus: AssertJ ships with spring-boot-starter-test. Fluent, readable
	// assertions that pair well with Mockito.
	// ------------------------------------------------------------------

	@Test
	void assertj_readableAssertions() {
		when(dataServiceMock.retrieveAllData()).thenReturn(new int[] {25, 15, 5});

		int result = businessImpl.findTheGreatestFromAllData();

		assertThat(result).isEqualTo(25).isPositive();
	}
}
