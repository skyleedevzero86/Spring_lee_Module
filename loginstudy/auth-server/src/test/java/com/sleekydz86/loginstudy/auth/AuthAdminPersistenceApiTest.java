package com.sleekydz86.loginstudy.auth;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sleekydz86.loginstudy.auth.config.AuthorizationServerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class AuthAdminPersistenceApiTest extends AuthServerIntegrationTestSupport {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void adminCanReadPersistenceHealthAndUserDtoWithoutPassword() throws Exception {
		mockMvc.perform(get("/api/admin/persistence/health").with(user("admin").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.registeredClientRepositoryType").value("JdbcRegisteredClientRepository"))
				.andExpect(jsonPath("$.authorizationServiceType").value("JdbcOAuth2AuthorizationService"))
				.andExpect(jsonPath("$.authorizationConsentServiceType").value("JdbcOAuth2AuthorizationConsentService"))
				.andExpect(jsonPath("$.userCount").isNumber())
				.andExpect(jsonPath("$.registeredClientCount").isNumber());

		mockMvc.perform(get("/api/admin/users/user").with(user("admin").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("user"))
				.andExpect(jsonPath("$.email").value("user@loginstudy.local"))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.roles").isArray());

		mockMvc.perform(get("/api/admin/clients/{clientId}", AuthorizationServerConfig.CLIENT_USER_PORTAL)
						.with(user("admin").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.clientId").value(AuthorizationServerConfig.CLIENT_USER_PORTAL))
				.andExpect(jsonPath("$.scopes").isArray());
	}

	@Test
	void regularUserCannotAccessAdminPersistenceApi() throws Exception {
		mockMvc.perform(get("/api/admin/persistence/health").with(user("user").roles("USER")))
				.andExpect(status().isForbidden());
	}

	@Test
	void unknownUserReturnsProblemDetail() throws Exception {
		mockMvc.perform(get("/api/admin/users/missing-user").with(user("admin").roles("ADMIN")))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("리소스를 찾을 수 없음"))
				.andExpect(jsonPath("$.detail").value("사용자를 찾을 수 없습니다: missing-user"));
	}
}
