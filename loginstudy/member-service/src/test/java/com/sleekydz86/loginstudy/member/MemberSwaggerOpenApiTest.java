package com.sleekydz86.loginstudy.member;

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
class MemberSwaggerOpenApiTest extends MemberRedisTestSupport {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("OpenAPI 문서는 공개되며 회원 API를 설명한다")
	void openApiDocumentIsPublicAndDescribesMemberApis() throws Exception {
		// given
		String openApiPath = "/v3/api-docs";

		// when
		var result = mockMvc.perform(get(openApiPath));

		// then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("LoginStudy Member Service API"))
				.andExpect(jsonPath("$.paths['/api/members/me']").exists())
				.andExpect(jsonPath("$.paths['/api/admin/members']").exists())
				.andExpect(jsonPath("$.paths['/api/admin/members/keyset']").exists())
				.andExpect(jsonPath("$.components.securitySchemes['bearer-jwt']").exists());
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
