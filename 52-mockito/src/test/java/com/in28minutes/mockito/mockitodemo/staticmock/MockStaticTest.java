package com.in28minutes.mockito.mockitodemo.staticmock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

//Static methods used to need PowerMock. Not any more.
//Mockito 5 makes the inline mock maker the default, so mockStatic() is built in -
//no extra dependency, no PowerMock, and it mocks final classes and methods too.
//The mock is scoped: it lives inside the try block, on this thread only. Always close it.
class MockStaticTest {

	@Test
	void mockStatic_freezeTheClock() {
		LocalDate fixedDate = LocalDate.of(2000, 1, 1);		//built before the mock exists

		try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
			mockedLocalDate.when(LocalDate::now).thenReturn(fixedDate);

			assertThat(LocalDate.now()).isEqualTo(fixedDate);
			mockedLocalDate.verify(LocalDate::now);			//statics can be verified as well
		}

		assertThat(LocalDate.now()).isNotEqualTo(fixedDate);	//real behaviour is back
	}

}
