package com.sleekydz86.loginstudy.member;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class MemberServiceApplicationTests extends MemberRedisTestSupport {

	@Test
	void contextLoads() {
	}
}
