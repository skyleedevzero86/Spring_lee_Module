package com.sleekydz86.loginstudy.userportal.config;

import java.io.InvalidClassException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.data.redis.RedisSessionRepository;

@Configuration
public class TolerantSessionRepositoryConfig {

	private static final Logger log = LoggerFactory.getLogger(TolerantSessionRepositoryConfig.class);

	@Bean
	static BeanPostProcessor tolerantSessionRepositoryPostProcessor() {
		return new BeanPostProcessor() {
			@Override
			public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
				if (bean instanceof RedisSessionRepository || bean instanceof RedisIndexedSessionRepository) {
					@SuppressWarnings({"rawtypes", "unchecked"})
					SessionRepository<Session> repository = (SessionRepository) bean;
					return wrap(repository);
				}
				return bean;
			}
		};
	}

	private static SessionRepository<Session> wrap(SessionRepository<Session> delegate) {
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
				catch (RuntimeException ex) {
					if (!isIncompatibleSession(ex)) {
						throw ex;
					}
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

	private static boolean isIncompatibleSession(Throwable ex) {
		for (Throwable current = ex; current != null; current = current.getCause()) {
			if (current instanceof SerializationException || current instanceof InvalidClassException
					|| current instanceof ClassNotFoundException) {
				return true;
			}
			if (current.getCause() == current) {
				break;
			}
		}
		String message = NestedExceptionUtils.getMostSpecificCause(ex).getMessage();
		return message != null && message.contains("InvalidClassException");
	}
}
