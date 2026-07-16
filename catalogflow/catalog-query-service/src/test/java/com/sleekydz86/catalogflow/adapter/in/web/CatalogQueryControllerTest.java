package com.sleekydz86.catalogflow.adapter.in.web;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.sleekydz86.catalogflow.adapter.out.persistence.ProductViewMongoRepository;
import com.sleekydz86.catalogflow.application.model.ProductView;
import com.sleekydz86.catalogflow.application.port.out.ProductViewStore;
import com.sleekydz86.catalogflow.global.config.CorrelationIdFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
class CatalogQueryControllerTest {

	private static final UUID CATEGORY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID SUPPLIER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

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
	private WebApplicationContext webApplicationContext;

	@Autowired
	private ProductViewStore productViewStore;

	@Autowired
	private ProductViewMongoRepository productViewMongoRepository;

	@MockitoBean
	private RabbitTemplate rabbitTemplate;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		productViewMongoRepository.deleteAll();
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
				.addFilters(webApplicationContext.getBean(CorrelationIdFilter.class))
				.build();
	}

	@Test
	@DisplayName("상품 상세 조회 API는 상품 정보를 반환한다")
	void shouldGetProductDetail() throws Exception {
		// given
		UUID productId = UUID.randomUUID();
		productViewStore.save(sampleProduct(
				productId,
				"\uBB34\uC120 \uD0A4\uBCF4\uB4DC",
				"PUBLISHED",
				Instant.parse("2026-07-16T12:00:00Z")));

		// when / then
		mockMvc.perform(get("/api/v1/catalog/products/{productId}", productId)
						.header(CorrelationIdFilter.CORRELATION_ID_HEADER, "corr-query-001"))
				.andExpect(status().isOk())
				.andExpect(header().string(CorrelationIdFilter.CORRELATION_ID_HEADER, "corr-query-001"))
				.andExpect(jsonPath("$.name", is("\uBB34\uC120 \uD0A4\uBCF4\uB4DC")))
				.andExpect(jsonPath("$.status", is("PUBLISHED")))
				.andExpect(jsonPath("$.supplierName", is("\uAE30\uBCF8 \uACF5\uAE09\uC0AC")));
	}

	@Test
	@DisplayName("없는 상품 조회는 404를 반환한다")
	void shouldReturnNotFoundForMissingProduct() throws Exception {
		// given
		UUID productId = UUID.randomUUID();

		// when / then
		mockMvc.perform(get("/api/v1/catalog/products/{productId}", productId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title", is("\uC0C1\uD488\uC744 \uCC3E\uC744 \uC218 \uC5C6\uC74C")));
	}

	@Test
	@DisplayName("목록·검색·카테고리·인기 상품 조회 API가 동작한다")
	void shouldListAndSearchProducts() throws Exception {
		// given
		UUID firstId = UUID.randomUUID();
		UUID secondId = UUID.randomUUID();
		productViewStore.save(sampleProduct(
				firstId,
				"\uBB34\uC120 \uD0A4\uBCF4\uB4DC",
				"PUBLISHED",
				Instant.parse("2026-07-16T12:00:00Z")));
		productViewStore.save(sampleProduct(
				secondId,
				"\uC720\uC120 \uB9C8\uC6B0\uC2A4",
				"PUBLISHED",
				Instant.parse("2026-07-16T11:00:00Z")));

		// when / then
		mockMvc.perform(get("/api/v1/catalog/products")
						.param("status", "PUBLISHED")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items", hasSize(2)))
				.andExpect(jsonPath("$.hasNext", is(false)));

		mockMvc.perform(get("/api/v1/catalog/products/search")
						.param("name", "\uD0A4\uBCF4\uB4DC")
						.param("status", "PUBLISHED"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items", hasSize(1)))
				.andExpect(jsonPath("$.items[0].name", is("\uBB34\uC120 \uD0A4\uBCF4\uB4DC")));

		mockMvc.perform(get("/api/v1/catalog/categories/{categoryId}/products", CATEGORY_ID)
						.param("status", "PUBLISHED"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items", hasSize(2)));

		mockMvc.perform(get("/api/v1/catalog/products/popular").param("size", "5"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items", hasSize(2)));
	}

	private ProductView sampleProduct(UUID productId, String name, String status, Instant publishedAt) {
		ProductView view = ProductView.create(productId);
		view.setName(name);
		view.setSummary(name);
		view.setDescription(name + " \uC124\uBA85");
		view.setPrice(new BigDecimal("59000"));
		view.setCurrency("KRW");
		view.setStatus(status);
		view.setCategoryId(CATEGORY_ID);
		view.setSupplierId(SUPPLIER_ID);
		view.setSupplierName("\uAE30\uBCF8 \uACF5\uAE09\uC0AC");
		view.setImageUrls(List.of("products/" + productId + "/image-1.jpg"));
		view.setKeywords(List.of("\uD0A4\uBCF4\uB4DC"));
		view.setTags(List.of("\uC804\uC790\uC81C\uD488"));
		view.setAiGenerated(false);
		view.setPublishedAt(publishedAt);
		view.setCreatedAt(publishedAt);
		view.setUpdatedAt(publishedAt);
		view.setVersion(1L);
		return view;
	}
}
