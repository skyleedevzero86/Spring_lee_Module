package com.sleekydz86.loginstudy.auth.config;

import com.sleekydz86.loginstudy.auth.service.AuthPersistenceQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class JdbcPersistenceGuard implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(JdbcPersistenceGuard.class);

	private final AuthPersistenceQueryService authPersistenceQueryService;

	public JdbcPersistenceGuard(AuthPersistenceQueryService authPersistenceQueryService) {
		this.authPersistenceQueryService = authPersistenceQueryService;
	}

	@Override
	public void run(ApplicationArguments args) {
		authPersistenceQueryService.assertJdbcBackedServices();
		log.info("JDBC 기반 OAuth2 영속 빈을 확인했습니다. InMemory 저장소는 사용되지 않습니다.");
	}
}
