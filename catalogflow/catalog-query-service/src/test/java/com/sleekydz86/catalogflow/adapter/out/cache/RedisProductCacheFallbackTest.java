package com.sleekydz86.catalogflow.adapter.out.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import com.sleekydz86.catalogflow.application.model.ProductView;
import com.sleekydz86.catalogflow.global.config.CacheProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.json.JsonMapper;

class RedisProductCacheFallbackTest {

	@Test
	@DisplayName("Redis 장애 시 캐시 조회는 비어 있는 결과로 폴백한다")
	void shouldFallbackToEmptyOnRedisReadFailure() {
		// given
		StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis 연결 실패"));
		RedisProductCacheAdapter adapter = new RedisProductCacheAdapter(
				redisTemplate,
				JsonMapper.builder().build(),
				new CacheProperties());

		// when
		Optional<ProductView> result = adapter.getProduct(UUID.randomUUID());

		// then
		assertTrue(result.isEmpty());
	}

	@Test
	@DisplayName("Redis 장애 시 캐시 저장 실패는 예외를 전파하지 않는다")
	void shouldSwallowRedisWriteFailure() {
		// given
		StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		doThrow(new RuntimeException("Redis 쓰기 실패"))
				.when(valueOperations)
				.set(anyString(), anyString(), any(Duration.class));
		RedisProductCacheAdapter adapter = new RedisProductCacheAdapter(
				redisTemplate,
				JsonMapper.builder().build(),
				new CacheProperties());
		ProductView view = ProductView.create(UUID.randomUUID());
		view.setName("폴백상품");

		// when / then
		adapter.putProduct(view);
		assertEquals("폴백상품", view.getName());
	}
}
