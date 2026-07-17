package com.sleekydz86.loginstudy.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class AuthSwaggerOpenApiTest extends AuthServerIntegrationTestSupport {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("OpenAPI 문서는 공개되며 인증 관리자 API를 설명한다")
	void openApiDocumentIsPublicAndDescribesAuthAdminApis() throws Exception {
		// given
		String openApiPath = "/v3/api-docs";

		// when
		var result = mockMvc.perform(get(openApiPath));

		// then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("LoginStudy Auth Server API"))
				.andExpect(jsonPath("$.paths['/api/admin/persistence/health']").exists())
				.andExpect(jsonPath("$.components.securitySchemes['auth-session']").exists());
	}

	@Test
	@DisplayName("Swagger UI에 공개적으로 접근할 수 있다")
	void swaggerUiIsPubliclyReachable() throws Exception {
		// given
		String swaggerUiPath = "/swagger-ui.html";

		// when
		var result = mockMvc.perform(get(swaggerUiPath));

		// then
		result.andExpect(status().is3xxRedirection());
	}
}
