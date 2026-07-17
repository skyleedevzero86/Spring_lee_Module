package com.sleekydz86.loginstudy.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sleekydz86.loginstudy.auth.config.AuthDataInitializer;
import com.sleekydz86.loginstudy.auth.config.AuthorizationServerConfig;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class ClientCredentialsTokenIntegrationTest extends AuthServerIntegrationTestSupport {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("클라이언트 자격 증명 토큰에 Bearer 유형과 액세스 토큰이 포함된다")
	void clientCredentialsTokenContainsBearerTypeAndAccessToken() throws Exception {
		// given
		String clientId = AuthorizationServerConfig.CLIENT_MEMBER_SERVICE;
		String clientSecret = AuthDataInitializer.MEMBER_SERVICE_SECRET;

		// when
		MvcResult result = mockMvc.perform(post("/oauth2/token")
						.with(httpBasic(clientId, clientSecret))
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("grant_type", "client_credentials")
						.param("scope", "member.read"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.access_token").isNotEmpty())
				.andExpect(jsonPath("$.token_type").value("Bearer"))
				.andReturn();

		// then
		@SuppressWarnings("unchecked")
		Map<String, Object> body = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
		String accessToken = String.valueOf(body.get("access_token"));
		String[] parts = accessToken.split("\\.");
		assertThat(parts).hasSize(3);

		String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
		assertThat(payloadJson).contains("\"iss\":\"http://localhost:9000\"");
		assertThat(payloadJson).contains("member-service");
		assertThat(payloadJson).contains("member.read");
	}
}
