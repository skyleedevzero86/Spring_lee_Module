package com.sleekydz86.loginstudy.auth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.data.redis.RedisSessionRepository;

@Configuration
public class TolerantSessionRepositoryConfig {

	private static final Logger log = LoggerFactory.getLogger(TolerantSessionRepositoryConfig.class);

	@Bean
	@Primary
	SessionRepository<Session> tolerantSessionRepository(RedisSessionRepository redisSessionRepository) {
		@SuppressWarnings({"rawtypes", "unchecked"})
		SessionRepository<Session> delegate = (SessionRepository) redisSessionRepository;
		return new SessionRepository<>() {
			@Override
			public Session createSession() {
				return delegate.createSession();
			}

			@Override
			public void save(Session session) {
				delegate.save(session);
			}

			@Override
			public Session findById(String id) {
				try {
					return delegate.findById(id);
				}
				catch (SerializationException ex) {
					log.warn("호환되지 않는 Redis 세션을 폐기합니다. sessionId={}, cause={}", id, ex.getMessage());
					delegate.deleteById(id);
					return null;
				}
			}

			@Override
			public void deleteById(String id) {
				delegate.deleteById(id);
			}
		};
	}
}
