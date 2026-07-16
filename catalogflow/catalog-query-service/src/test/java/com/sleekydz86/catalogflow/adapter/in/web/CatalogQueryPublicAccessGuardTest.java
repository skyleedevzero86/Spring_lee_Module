package com.sleekydz86.catalogflow.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sleekydz86.catalogflow.security.PublicAccessGuardFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
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
		"app.network.public-access-enabled=false",
		"app.network.actuator-access-enabled=true",
		"app.security.enabled=false",
		"management.endpoints.web.exposure.include=health,info,prometheus,metrics"
})
class CatalogQueryPublicAccessGuardTest {

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
	private PublicAccessGuardFilter publicAccessGuardFilter;

	@MockitoBean
	private RabbitTemplate rabbitTemplate;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
				.addFilters(publicAccessGuardFilter)
				.build();
	}

	@Test
	@DisplayName("Query 서비스는 외부 API 직접 접근을 거부하고 메인 포트 안내를 반환한다")
	void shouldRejectPublicApiAccess() throws Exception {
		// given / when / then
		mockMvc.perform(get("/api/v1/catalog/products/popular"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.title").value("외부 직접 접근이 거부되었습니다"))
				.andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("8081")));
	}

	@Test
	@DisplayName("Query 서비스 Actuator Health는 모니터링을 위해 허용한다")
	void shouldAllowActuatorHealth() throws Exception {
		// given / when / then
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk());
	}
}
