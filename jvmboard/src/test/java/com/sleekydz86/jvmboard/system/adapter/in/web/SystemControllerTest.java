package com.sleekydz86.jvmboard.system.adapter.in.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sleekydz86.jvmboard.system.application.port.in.GetSystemSnapshotUseCase;
import com.sleekydz86.jvmboard.system.domain.SystemSnapshot;

@WebMvcTest(SystemController.class)
class SystemControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GetSystemSnapshotUseCase getSystemSnapshotUseCase;

	@Test
	void returnsTheDashboardSnapshot() throws Exception {
		given(getSystemSnapshotUseCase.get()).willReturn(new SystemSnapshot(
			27,
			"OpenJDK 64-Bit Server VM",
			8,
			324L * 1024 * 1024,
			4096L * 1024 * 1024,
			"G1",
			Duration.ofMinutes(12).plusSeconds(41)
		));

		mockMvc.perform(get("/api/system"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.javaVersion").value("27"))
			.andExpect(jsonPath("$.jvm").value("OpenJDK 64-Bit Server VM"))
			.andExpect(jsonPath("$.cpu").value("8개 프로세서"))
			.andExpect(jsonPath("$.heapUsed").value("324 MB"))
			.andExpect(jsonPath("$.heapMax").value("4096 MB"))
			.andExpect(jsonPath("$.garbageCollector").value("G1"))
			.andExpect(jsonPath("$.uptime").value("00:12:41"));
	}
}
