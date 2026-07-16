package com.sleekydz86.catalogflow.adapter.out.cache;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.sleekydz86.catalogflow.application.model.ProductView;
import com.sleekydz86.catalogflow.application.port.out.ProductCachePort;
import com.sleekydz86.catalogflow.application.query.ProductPageResult;
import com.sleekydz86.catalogflow.global.cache.CacheKeys;
import com.sleekydz86.catalogflow.global.config.CacheProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(name = "app.cache.enabled", havingValue = "true", matchIfMissing = true)
public class RedisProductCacheAdapter implements ProductCachePort {

	private static final String NULL_MARKER = "{\"__null\":true}";

	private final StringRedisTemplate stringRedisTemplate;
	private final ObjectMapper cacheObjectMapper;
	private final CacheProperties cacheProperties;

	public RedisProductCacheAdapter(
			StringRedisTemplate stringRedisTemplate,
			@Qualifier("cacheObjectMapper") ObjectMapper cacheObjectMapper,
			CacheProperties cacheProperties) {
		this.stringRedisTemplate = stringRedisTemplate;
		this.cacheObjectMapper = cacheObjectMapper;
		this.cacheProperties = cacheProperties;
	}

	@Override
	public Optional<ProductView> getProduct(UUID productId) {
		try {
			String value = stringRedisTemplate.opsForValue().get(CacheKeys.product(productId));
			if (value == null || NULL_MARKER.equals(value)) {
				return Optional.empty();
			}
			return Optional.of(cacheObjectMapper.readValue(value, ProductView.class));
		}
		catch (Exception exception) {
			return Optional.empty();
		}
	}

	@Override
	public void putProduct(ProductView productView) {
		try {
			String json = cacheObjectMapper.writeValueAsString(productView);
			stringRedisTemplate.opsForValue().set(
					CacheKeys.product(productView.getProductId()),
					json,
					Duration.ofSeconds(cacheProperties.getProductTtlSeconds()));
		}
		catch (Exception ignored) {
		}
	}

	@Override
	public void putProductMiss(UUID productId) {
		try {
			stringRedisTemplate.opsForValue().set(
					CacheKeys.product(productId),
					NULL_MARKER,
					Duration.ofSeconds(cacheProperties.getNullTtlSeconds()));
		}
		catch (Exception ignored) {
		}
	}

	@Override
	public boolean isProductMiss(UUID productId) {
		try {
			String value = stringRedisTemplate.opsForValue().get(CacheKeys.product(productId));
			return NULL_MARKER.equals(value);
		}
		catch (Exception exception) {
			return false;
		}
	}

	@Override
	public Optional<ProductPageResult> getCategoryPage(UUID categoryId, String status, String cursor, int size) {
		return readPage(CacheKeys.categoryPage(categoryId, status, cursor, size));
	}

	@Override
	public void putCategoryPage(
			UUID categoryId,
			String status,
			String cursor,
			int size,
			ProductPageResult pageResult) {
		writePage(
				CacheKeys.categoryPage(categoryId, status, cursor, size),
				pageResult,
				Duration.ofSeconds(cacheProperties.getCategoryTtlSeconds()));
	}

	@Override
	public Optional<ProductPageResult> getPopular(int size) {
		return readPage(CacheKeys.popular(size));
	}

	@Override
	public void putPopular(int size, ProductPageResult pageResult) {
		writePage(
				CacheKeys.popular(size),
				pageResult,
				Duration.ofSeconds(cacheProperties.getPopularTtlSeconds()));
	}

	@Override
	public void evictProductRelated(UUID productId, UUID categoryId) {
		try {
			stringRedisTemplate.delete(CacheKeys.product(productId));
			evictByPrefix(CacheKeys.popularPrefix());
			if (categoryId != null) {
				evictByPrefix(CacheKeys.categoryPrefix(categoryId));
			}
		}
		catch (Exception ignored) {
		}
	}

	private Optional<ProductPageResult> readPage(String key) {
		try {
			String value = stringRedisTemplate.opsForValue().get(key);
			if (value == null) {
				return Optional.empty();
			}
			return Optional.of(cacheObjectMapper.readValue(value, ProductPageResult.class));
		}
		catch (Exception exception) {
			return Optional.empty();
		}
	}

	private void writePage(String key, ProductPageResult pageResult, Duration ttl) {
		try {
			String json = cacheObjectMapper.writeValueAsString(pageResult);
			stringRedisTemplate.opsForValue().set(key, json, ttl);
		}
		catch (Exception ignored) {
		}
	}

	private void evictByPrefix(String prefix) {
		Set<String> keys = stringRedisTemplate.keys(prefix + "*");
		if (keys != null && !keys.isEmpty()) {
			stringRedisTemplate.delete(keys);
		}
	}
}
