package com.in28minutes.mockito.mockitodemo.business;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

//@Mock gives you a mock in a plain JUnit test.
//@MockitoBean puts that mock INSIDE the Spring context, replacing (or creating) the bean.
//Spring Boot 4 REMOVED @MockBean and @SpyBean - it is @MockitoBean and @MockitoSpyBean now,
//and they come from spring-test, not spring-boot-test. Watch the import.
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
