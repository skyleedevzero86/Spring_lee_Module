package com.sleekydz86.loginstudy.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sleekydz86.loginstudy.auth.config.AuthDataInitializer;
import com.sleekydz86.loginstudy.auth.config.AuthorizationServerConfig;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class AuthActuatorPrometheusTest extends AuthServerIntegrationTestSupport {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MeterRegistry meterRegistry;

	@Test
	void prometheusEndpointExposesCustomAuthCounters() throws Exception {
		// given
		mockMvc.perform(formLogin("/login").user("user").password(AuthDataInitializer.DEMO_USER_PASSWORD))
				.andExpect(authenticated());
		mockMvc.perform(formLogin("/login").user("user").password("wrong-password"))
				.andExpect(unauthenticated());
		mockMvc.perform(post("/oauth2/token")
						.with(httpBasic(
								AuthorizationServerConfig.CLIENT_MEMBER_SERVICE,
								AuthDataInitializer.MEMBER_SERVICE_SECRET))
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("grant_type", "client_credentials")
						.param("scope", "member.read"))
				.andExpect(status().isOk());

		// when
		var body = mockMvc.perform(get("/actuator/prometheus").accept(MediaType.TEXT_PLAIN))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
				.andReturn()
				.getResponse()
				.getContentAsString();

		// then
		assertThat(body).contains("loginstudy_auth_login_total");
		assertThat(body).contains("loginstudy_auth_token_total");
		assertThat(body).contains("jvm_memory_used_bytes");
		assertThat(meterRegistry.counter("loginstudy.auth.login", "result", "success").count()).isGreaterThan(0);
		assertThat(meterRegistry.counter("loginstudy.auth.login", "result", "failure").count()).isGreaterThan(0);
		assertThat(meterRegistry.counter("loginstudy.auth.token", "result", "success").count()).isGreaterThan(0);
	}
}
