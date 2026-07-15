package com.sleekydz86.loginstudy.adminportal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@Import(AdminPortalOAuth2TestConfig.class)
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
class AdminPortalActuatorPrometheusTest extends RedisTestSupport {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void prometheusEndpointIsPublic() throws Exception {
		// given
		String prometheusPath = "/actuator/prometheus";

		// when
		var result = mockMvc.perform(get(prometheusPath));

		// then
		result.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("jvm_memory_used_bytes")))
				.andExpect(content().string(Matchers.containsString("application=\"admin-portal\"")));
	}
}
