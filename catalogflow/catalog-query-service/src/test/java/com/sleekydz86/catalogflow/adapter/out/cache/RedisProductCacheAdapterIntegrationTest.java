package com.sleekydz86.catalogflow.adapter.out.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.sleekydz86.catalogflow.application.model.ProductView;
import com.sleekydz86.catalogflow.application.port.out.ProductCachePort;
import com.sleekydz86.catalogflow.application.query.ProductPageResult;
import com.sleekydz86.catalogflow.global.cache.CacheKeys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
class RedisProductCacheAdapterIntegrationTest {

	@Container
	@ServiceConnection
	static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

	@Container
	@ServiceConnection
	static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:4-management-alpine");

	@Container
	@ServiceConnection(name = "redis")
	static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

	@Autowired
	private ProductCachePort productCachePort;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Test
	void shouldCacheProductAndEvictRelatedKeys() {
		UUID productId = UUID.randomUUID();
		UUID categoryId = UUID.randomUUID();
		ProductView view = ProductView.create(productId);
		view.setName("\uCE90\uC2DC \uC0C1\uD488");
		view.setPrice(new BigDecimal("10000"));
		view.setCurrency("KRW");
		view.setStatus("PUBLISHED");
		view.setCategoryId(categoryId);
		view.setPublishedAt(Instant.parse("2026-07-16T12:00:00Z"));
		view.setCreatedAt(Instant.parse("2026-07-16T12:00:00Z"));
		view.setUpdatedAt(Instant.parse("2026-07-16T12:00:00Z"));
		view.setVersion(1L);

		productCachePort.putProduct(view);
		productCachePort.putPopular(10, new ProductPageResult(List.of(view), null, false));
		productCachePort.putCategoryPage(
				categoryId,
				"PUBLISHED",
				null,
				20,
				new ProductPageResult(List.of(view), null, false));

		assertTrue(productCachePort.getProduct(productId).isPresent());
		assertEquals("\uCE90\uC2DC \uC0C1\uD488", productCachePort.getProduct(productId).orElseThrow().getName());
		assertTrue(productCachePort.getPopular(10).isPresent());
		assertTrue(productCachePort.getCategoryPage(categoryId, "PUBLISHED", null, 20).isPresent());

		productCachePort.evictProductRelated(productId, categoryId);

		assertTrue(productCachePort.getProduct(productId).isEmpty());
		assertTrue(productCachePort.getPopular(10).isEmpty());
		assertTrue(productCachePort.getCategoryPage(categoryId, "PUBLISHED", null, 20).isEmpty());
		assertTrue(stringRedisTemplate.keys(CacheKeys.popularPrefix() + "*").isEmpty());
	}

	@Test
	void shouldCacheProductMissMarker() {
		UUID productId = UUID.randomUUID();
		productCachePort.putProductMiss(productId);
		assertTrue(productCachePort.isProductMiss(productId));
		assertTrue(productCachePort.getProduct(productId).isEmpty());
	}
}
