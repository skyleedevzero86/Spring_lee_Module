package com.sleekydz86.loginstudy.userportal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(UserPortalOAuth2TestConfig.class)
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
class UserPortalApplicationTests extends RedisTestSupport {

	@Test
	void contextLoads() {
	}
}
