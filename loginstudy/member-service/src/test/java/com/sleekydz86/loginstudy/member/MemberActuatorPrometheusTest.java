package com.sleekydz86.loginstudy.member;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
class MemberActuatorPrometheusTest extends MemberRedisTestSupport {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("Prometheus 엔드포인트는 공개되며 JVM과 HTTP 지표를 제공한다")
	void prometheusEndpointIsPublicAndExposesJvmAndHttpMetrics() throws Exception {
		// given
		assertThatPrometheusRegistryIsPresent();
		String prometheusPath = "/actuator/prometheus";

		// when
		var result = mockMvc.perform(get(prometheusPath));

		// then
		result.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("jvm_memory_used_bytes")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("application=\"member-service\"")));
	}

	private static void assertThatPrometheusRegistryIsPresent() throws ClassNotFoundException {
		Class.forName("io.micrometer.prometheusmetrics.PrometheusMeterRegistry");
	}
}
