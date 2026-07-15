package com.sleekydz86.loginstudy.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class AuthServerApplicationTests extends AuthServerIntegrationTestSupport {

	@Test
	void contextLoads() {
	}
}
