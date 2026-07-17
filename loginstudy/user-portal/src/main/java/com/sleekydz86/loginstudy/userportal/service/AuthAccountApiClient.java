package com.sleekydz86.loginstudy.userportal.service;

import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.ObjectMapper;

@Service
public class AuthAccountApiClient {

	private final RestClient authApiRestClient;
	private final ObjectMapper objectMapper;

	public AuthAccountApiClient(
			@Qualifier("authApiRestClient") RestClient authApiRestClient,
			ObjectMapper objectMapper) {
		this.authApiRestClient = authApiRestClient;
		this.objectMapper = objectMapper;
	}

	public ApiCallResult getOwnProfile(String accessToken) {
		return exchange(accessToken, null);
	}

	public ApiCallResult updateOwnProfile(
			String accessToken,
			String displayName,
			String email,
			String phone) {
		try {
			String body = objectMapper.writeValueAsString(Map.of(
					"displayName", displayName,
					"email", email,
					"phone", phone));
			return exchange(accessToken, body);
		}
		catch (Exception ex) {
			return ApiCallResult.failure(0, ex.getMessage());
		}
	}

	private ApiCallResult exchange(String accessToken, String requestBody) {
		try {
			RestClient.RequestHeadersSpec<?> request = requestBody == null
					? authApiRestClient.get().uri("/api/account/me")
					: authApiRestClient.patch()
							.uri("/api/account/me")
							.contentType(MediaType.APPLICATION_JSON)
							.body(requestBody);
			String body = request
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
					.header("X-Correlation-Id", UUID.randomUUID().toString())
					.retrieve()
					.body(String.class);
			return ApiCallResult.success(body == null ? "" : body);
		}
		catch (RestClientResponseException ex) {
			return ApiCallResult.failure(ex.getStatusCode().value(), ex.getResponseBodyAsString());
		}
		catch (Exception ex) {
			return ApiCallResult.failure(0, ex.getMessage());
		}
	}

	public record ApiCallResult(boolean success, int statusCode, String body) {

		public static ApiCallResult success(String body) {
			return new ApiCallResult(true, 200, body);
		}

		public static ApiCallResult failure(int statusCode, String body) {
			return new ApiCallResult(false, statusCode, body == null ? "" : body);
		}
	}
}
