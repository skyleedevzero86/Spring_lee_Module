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
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

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
	void shouldGetProductDetail() throws Exception {
		UUID productId = UUID.randomUUID();
		productViewStore.save(sampleProduct(
				productId,
				"무선 키보드",
				"PUBLISHED",
				Instant.parse("2026-07-16T12:00:00Z")));

		mockMvc.perform(get("/api/v1/catalog/products/{productId}", productId)
						.header(CorrelationIdFilter.CORRELATION_ID_HEADER, "corr-query-001"))
				.andExpect(status().isOk())
				.andExpect(header().string(CorrelationIdFilter.CORRELATION_ID_HEADER, "corr-query-001"))
				.andExpect(jsonPath("$.name", is("무선 키보드")))
				.andExpect(jsonPath("$.status", is("PUBLISHED")))
				.andExpect(jsonPath("$.supplierName", is("기본 공급사")));
	}

	@Test
	void shouldReturnNotFoundForMissingProduct() throws Exception {
		UUID productId = UUID.randomUUID();

		mockMvc.perform(get("/api/v1/catalog/products/{productId}", productId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title", is("상품을 찾을 수 없음")));
	}

	@Test
	void shouldListAndSearchProducts() throws Exception {
		UUID firstId = UUID.randomUUID();
		UUID secondId = UUID.randomUUID();
		productViewStore.save(sampleProduct(
				firstId,
				"무선 키보드",
				"PUBLISHED",
				Instant.parse("2026-07-16T12:00:00Z")));
		productViewStore.save(sampleProduct(
				secondId,
				"유선 마우스",
				"PUBLISHED",
				Instant.parse("2026-07-16T11:00:00Z")));

		mockMvc.perform(get("/api/v1/catalog/products")
						.param("status", "PUBLISHED")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items", hasSize(2)))
				.andExpect(jsonPath("$.hasNext", is(false)));

		mockMvc.perform(get("/api/v1/catalog/products/search")
						.param("name", "키보드")
						.param("status", "PUBLISHED"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items", hasSize(1)))
				.andExpect(jsonPath("$.items[0].name", is("무선 키보드")));

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
		view.setDescription(name + " 설명");
		view.setPrice(new BigDecimal("59000"));
		view.setCurrency("KRW");
		view.setStatus(status);
		view.setCategoryId(CATEGORY_ID);
		view.setSupplierId(SUPPLIER_ID);
		view.setSupplierName("기본 공급사");
		view.setImageUrls(List.of("products/" + productId + "/image-1.jpg"));
		view.setKeywords(List.of("키보드"));
		view.setTags(List.of("전자제품"));
		view.setAiGenerated(false);
		view.setPublishedAt(publishedAt);
		view.setCreatedAt(publishedAt);
		view.setUpdatedAt(publishedAt);
		view.setVersion(1L);
		return view;
	}
}
