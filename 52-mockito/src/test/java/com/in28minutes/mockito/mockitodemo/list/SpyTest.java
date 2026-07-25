package com.in28minutes.mockito.mockitodemo.list;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

//A mock is an empty shell - it forgets everything unless you stub it.
//A spy is the REAL object - it keeps working, and you can still verify and stub it.
//Rule of thumb: reach for a mock first. Use a spy for legacy code you cannot redesign.
class SpyTest {

	@Test
	void mock_forgetsEverything() {
		List<String> listMock = mock();				//Mockito 5 infers the type - no List.class

		listMock.add("SomeString");

		assertThat(listMock.size()).isZero();			//the add() went nowhere
		assertThat(listMock.get(0)).isNull();
	}

	@Test
	void spy_keepsTheRealBehaviour() {
		List<String> listSpy = spy(new ArrayList<String>());

		listSpy.add("SomeString");

		assertThat(listSpy.size()).isEqualTo(1);		//a real ArrayList did the work
		assertThat(listSpy.get(0)).isEqualTo("SomeString");
		verify(listSpy).add("SomeString");				//and it is still a Mockito mock
	}

	@Test
	void spy_canStillBeStubbed() {
		List<String> listSpy = spy(new ArrayList<String>());
		listSpy.add("SomeString");

		when(listSpy.size()).thenReturn(10);			//stubbing wins over real behaviour

		assertThat(listSpy.size()).isEqualTo(10);
		assertThat(listSpy.get(0)).isEqualTo("SomeString");	//everything else stays real
	}

}
