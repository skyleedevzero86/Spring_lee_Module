package com.sleekydz86.catalogflow.adapter.in.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.sleekydz86.catalogflow.security.CatalogRoles;
import com.sleekydz86.catalogflow.security.CatalogScopes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
		"app.security.enabled=true",
		"app.security.jwt-decoder-mode=symmetric",
		"app.security.audience=catalogflow-api",
		"app.security.symmetric-secret=catalogflow-test-symmetric-secret-key-32bytes",
		"app.network.public-access-enabled=true"
})
class ProductCommandSecurityTest {

	private static final UUID CATEGORY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID SUPPLIER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

	@Autowired
	private WebApplicationContext webApplicationContext;

	@MockitoBean
	private RabbitTemplate rabbitTemplate;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
				.apply(springSecurity())
				.build();
	}

	@Test
	@DisplayName("토큰 없이 상품 등록을 요청하면 401을 반환한다")
	void shouldRejectUnauthenticatedCreate() throws Exception {
		// given
		String body = createBody();

		// when / then
		mockMvc.perform(post("/api/v1/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.title").value("인증이 필요합니다"));
	}

	@Test
	@DisplayName("catalog.write 스코프로 상품을 등록할 수 있다")
	void shouldAllowCreateWithWriteScope() throws Exception {
		// given
		String body = createBody();

		// when / then
		mockMvc.perform(post("/api/v1/products")
						.with(jwt().authorities(new SimpleGrantedAuthority(CatalogScopes.SCOPE_WRITE)))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated());
	}

	@Test
	@DisplayName("catalog.read 스코프만 있으면 상품 등록이 거부된다")
	void shouldRejectCreateWithReadScopeOnly() throws Exception {
		// given
		String body = createBody();

		// when / then
		mockMvc.perform(post("/api/v1/products")
						.with(jwt().authorities(new SimpleGrantedAuthority(CatalogScopes.SCOPE_READ)))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.title").value("접근이 거부되었습니다"));
	}

	@Test
	@DisplayName("SYSTEM_ADMIN 역할로 시스템 API에 접근할 수 있다")
	void shouldAllowSystemDocsForAdmin() throws Exception {
		// given / when / then
		mockMvc.perform(get("/api/v1/system")
						.with(jwt().authorities(new SimpleGrantedAuthority(CatalogRoles.ROLE_SYSTEM_ADMIN))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("main"));
	}

	@Test
	@DisplayName("일반 편집자 역할은 시스템 API에 접근할 수 없다")
	void shouldRejectSystemDocsForEditor() throws Exception {
		// given / when / then
		mockMvc.perform(get("/api/v1/system")
						.with(jwt().authorities(new SimpleGrantedAuthority(CatalogRoles.ROLE_EDITOR))))
				.andExpect(status().isForbidden());
	}

	private String createBody() {
		return """
				{
				  "name": "보안테스트상품",
				  "description": "설명",
				  "priceAmount": 1000,
				  "priceCurrency": "KRW",
				  "categoryId": "%s",
				  "supplierId": "%s"
				}
				""".formatted(CATEGORY_ID, SUPPLIER_ID);
	}
}
