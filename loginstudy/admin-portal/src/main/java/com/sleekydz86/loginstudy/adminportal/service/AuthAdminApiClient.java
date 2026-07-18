package com.sleekydz86.loginstudy.adminportal.service;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class AuthAdminApiClient {

	private static final Logger log = LoggerFactory.getLogger(AuthAdminApiClient.class);

	private final RestClient authApiRestClient;

	public AuthAdminApiClient(@Qualifier("authApiRestClient") RestClient authApiRestClient) {
		this.authApiRestClient = authApiRestClient;
	}

	public ApiCallResult listUsers(String accessToken) {
		return exchange(accessToken, "GET", "/api/admin/users", null);
	}

	public ApiCallResult changeRole(String accessToken, String username, String role) {
		return exchange(
				accessToken,
				"POST",
				"/api/admin/users/" + username + "/role",
				"{\"role\":\"" + role + "\"}");
	}

	public ApiCallResult changeStatus(String accessToken, String username, String status) {
		return exchange(
				accessToken,
				"POST",
				"/api/admin/users/" + username + "/status",
				"{\"status\":\"" + status + "\"}");
	}

	public ApiCallResult getOwnProfile(String accessToken) {
		return exchange(accessToken, "GET", "/api/account/me", null);
	}

	public ApiCallResult updateOwnProfile(
			String accessToken,
			String displayName,
			String email,
			String phone) {
		String body = "{\"displayName\":\"" + escapeJson(displayName)
				+ "\",\"email\":\"" + escapeJson(email)
				+ "\",\"phone\":\"" + escapeJson(phone) + "\"}";
		return exchange(accessToken, "PATCH", "/api/account/me", body);
	}

	private ApiCallResult exchange(
			String accessToken,
			String method,
			String path,
			String requestBody) {
		try {
			RestClient.RequestHeadersSpec<?> request = switch (method) {
				case "POST" -> authApiRestClient.post()
						.uri(path)
						.contentType(MediaType.APPLICATION_JSON)
						.body(requestBody);
				case "PATCH" -> authApiRestClient.patch()
						.uri(path)
						.contentType(MediaType.APPLICATION_JSON)
						.body(requestBody);
				default -> authApiRestClient.get().uri(path);
			};
			String body = request
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
					.header("X-Correlation-Id", UUID.randomUUID().toString())
					.retrieve()
					.body(String.class);
			return ApiCallResult.success(body == null ? "" : body);
		}
		catch (RestClientResponseException ex) {
			log.warn("인증 API 호출 실패: method={}, path={}, status={}",
					method, path, ex.getStatusCode().value());
			return ApiCallResult.failure(ex.getStatusCode().value(), ex.getResponseBodyAsString());
		}
		catch (Exception ex) {
			log.warn("인증 API 연결 실패: method={}, path={}, cause={}",
					method, path, ex.getMessage());
			return ApiCallResult.failure(0, ex.getMessage());
		}
	}

	private static String escapeJson(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
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
