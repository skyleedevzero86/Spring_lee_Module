package com.sleekydz86.loginstudy.adminportal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@Import(AdminPortalOAuth2TestConfig.class)
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
class AdminPortalSwaggerOpenApiTest extends RedisTestSupport {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void openApiDocumentIsPublic() throws Exception {
		// given
		String openApiPath = "/v3/api-docs";

		// when
		var result = mockMvc.perform(get(openApiPath));

		// then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("LoginStudy Admin Portal"))
				.andExpect(jsonPath("$.paths['/']").exists())
				.andExpect(jsonPath("$.paths['/admin']").exists());
	}
}
