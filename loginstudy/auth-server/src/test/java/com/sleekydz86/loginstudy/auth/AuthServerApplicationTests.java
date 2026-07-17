package com.sleekydz86.loginstudy.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class AuthServerApplicationTests extends AuthServerIntegrationTestSupport {

	@Test
	@DisplayName("애플리케이션 컨텍스트가 정상적으로 로드된다")
	void contextLoads() {
		// when

		// then
	}
}
