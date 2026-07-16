package com.sleekydz86.catalogflow.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.json.JsonMapper;

class PublicAccessGuardFilterTest {

	@Test
	@DisplayName("공개 접근이 허용되면 요청을 그대로 통과시킨다")
	void shouldPassThroughWhenPublicAccessEnabled() throws Exception {
		// given
		CatalogNetworkProperties properties = new CatalogNetworkProperties();
		properties.setPublicAccessEnabled(true);
		PublicAccessGuardFilter filter = new PublicAccessGuardFilter(properties, JsonMapper.builder().build());
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);

		// when
		filter.doFilter(request, response, chain);

		// then
		verify(chain).doFilter(request, response);
		verifyNoInteractions(response);
	}

	@Test
	@DisplayName("내부 서비스는 API 직접 접근을 한국어 메시지로 거부한다")
	void shouldRejectApiAccessWhenPublicAccessDisabled() throws Exception {
		// given
		CatalogNetworkProperties properties = new CatalogNetworkProperties();
		properties.setPublicAccessEnabled(false);
		properties.setMainServicePort(8081);
		properties.setMainServiceName("catalog-command-service");
		PublicAccessGuardFilter filter = new PublicAccessGuardFilter(properties, JsonMapper.builder().build());
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		when(request.getRequestURI()).thenReturn("/api/v1/catalog/products");
		when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
			@Override
			public void write(int b) {
				output.write(b);
			}

			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setWriteListener(WriteListener writeListener) {
			}
		});

		// when
		filter.doFilter(request, response, chain);

		// then
		verify(response).setStatus(HttpStatus.FORBIDDEN.value());
		assertTrue(output.toString().contains("외부에서 직접 접근할 수 없습니다"));
		assertTrue(output.toString().contains("8081"));
		verifyNoInteractions(chain);
	}

	@Test
	@DisplayName("내부 서비스라도 Actuator 경로는 모니터링을 위해 허용한다")
	void shouldAllowActuatorWhenConfigured() throws Exception {
		// given
		CatalogNetworkProperties properties = new CatalogNetworkProperties();
		properties.setPublicAccessEnabled(false);
		properties.setActuatorAccessEnabled(true);
		PublicAccessGuardFilter filter = new PublicAccessGuardFilter(properties, JsonMapper.builder().build());
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);
		when(request.getRequestURI()).thenReturn("/actuator/prometheus");

		// when
		filter.doFilter(request, response, chain);

		// then
		verify(chain).doFilter(request, response);
		assertEquals(true, properties.isActuatorAccessEnabled());
	}
}
