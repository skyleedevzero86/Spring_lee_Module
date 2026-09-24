package com.sleekydz86.opspilot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class OpsPilotApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void triageCacheAndStats() throws Exception {
		String body = """
			{
			  "title": "DB 커넥션 고갈",
			  "description": "FATAL: connection timeout postgres pool exhaustion",
			  "type": "DATABASE",
			  "severity": "HIGH",
			  "source": "order-service"
			}
			""";

		mockMvc.perform(post("/api/incidents/triage")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.category").value("DATABASE"))
			.andExpect(jsonPath("$.cacheHit").value(false));

		mockMvc.perform(post("/api/incidents/triage")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.cacheHit").value(true));

		mockMvc.perform(get("/api/incidents?limit=10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].title").value("DB 커넥션 고갈"));

		mockMvc.perform(get("/api/stats").param("granularity", "HOUR"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalIncidents").value(2));
	}
}
