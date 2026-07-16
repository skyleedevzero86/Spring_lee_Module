package com.sleekydz86.catalogflow.adapter.in.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.sleekydz86.catalogflow.adapter.out.persistence.ProductViewMongoRepository;
import com.sleekydz86.catalogflow.application.model.ProductView;
import com.sleekydz86.catalogflow.application.port.out.ProductViewStore;
import com.sleekydz86.catalogflow.security.CatalogScopes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
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
@TestPropertySource(properties = {
		"app.security.enabled=true",
		"app.security.jwt-decoder-mode=symmetric",
		"app.security.audience=catalogflow-api",
		"app.network.public-access-enabled=true"
})
class CatalogQuerySecurityTest {

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
				.apply(springSecurity())
				.build();
	}

	@Test
	@DisplayName("토큰 없이 상품 조회를 요청하면 401을 반환한다")
	void shouldRejectUnauthenticatedQuery() throws Exception {
		// given
		UUID productId = UUID.randomUUID();
		productViewStore.save(sample(productId));

		// when / then
		mockMvc.perform(get("/api/v1/catalog/products/{productId}", productId))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("catalog.read 스코프로 상품을 조회할 수 있다")
	void shouldAllowQueryWithReadScope() throws Exception {
		// given
		UUID productId = UUID.randomUUID();
		productViewStore.save(sample(productId));

		// when / then
		mockMvc.perform(get("/api/v1/catalog/products/{productId}", productId)
						.with(jwt().authorities(new SimpleGrantedAuthority(CatalogScopes.SCOPE_READ))))
				.andExpect(status().isOk());
	}

	private ProductView sample(UUID productId) {
		ProductView view = ProductView.create(productId);
		view.setName("보안조회상품");
		view.setSummary("요약");
		view.setDescription("설명");
		view.setPrice(BigDecimal.valueOf(1000));
		view.setCurrency("KRW");
		view.setStatus("PUBLISHED");
		view.setCategoryId(CATEGORY_ID);
		view.setSupplierId(SUPPLIER_ID);
		view.setSupplierName("공급사");
		view.setImageUrls(List.of());
		view.setKeywords(List.of());
		view.setTags(List.of());
		view.setAiGenerated(false);
		view.setPublishedAt(Instant.parse("2026-07-16T00:00:00Z"));
		view.setCreatedAt(Instant.parse("2026-07-16T00:00:00Z"));
		view.setUpdatedAt(Instant.parse("2026-07-16T00:00:00Z"));
		view.setVersion(1L);
		return view;
	}
}
