package com.sleekydz86.loginstudy.adminportal.service;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class MemberAdminApiClient {

	private final RestClient memberApiRestClient;

	public MemberAdminApiClient(@Qualifier("memberApiRestClient") RestClient memberApiRestClient) {
		this.memberApiRestClient = memberApiRestClient;
	}

	public ApiCallResult listMembers(String accessToken) {
		return get(accessToken, "/api/admin/members?page=0&size=20");
	}

	public ApiCallResult getMember(String accessToken, long memberId) {
		return get(accessToken, "/api/admin/members/" + memberId);
	}

	public ApiCallResult revealSensitiveField(String accessToken, long memberId, String field) {
		String path = "/api/admin/members/" + memberId + "/sensitive/" + field + "/reveal";
		try {
			String body = memberApiRestClient.post()
					.uri(path)
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

	public ApiCallResult changeStatus(String accessToken, long memberId, String status) {
		return postJson(
				accessToken,
				"/api/admin/members/" + memberId + "/status",
				"{\"status\":\"" + status + "\",\"reason\":\"관리자 화면 변경\"}");
	}

	public ApiCallResult updateProfile(
			String accessToken,
			long memberId,
			long version,
			String displayName,
			String email) {
		String body = "{\"version\":" + version
				+ ",\"displayName\":\"" + escapeJson(displayName)
				+ "\",\"email\":\"" + escapeJson(email) + "\"}";
		return patchJson(accessToken, "/api/members/" + memberId, body);
	}

	private ApiCallResult get(String accessToken, String path) {
		try {
			String body = memberApiRestClient.get()
					.uri(path)
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

	private ApiCallResult postJson(String accessToken, String path, String requestBody) {
		try {
			String body = memberApiRestClient.post()
					.uri(path)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
					.header("X-Correlation-Id", UUID.randomUUID().toString())
					.contentType(MediaType.APPLICATION_JSON)
					.body(requestBody)
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

	private ApiCallResult patchJson(String accessToken, String path, String requestBody) {
		try {
			String body = memberApiRestClient.patch()
					.uri(path)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
					.header("X-Correlation-Id", UUID.randomUUID().toString())
					.contentType(MediaType.APPLICATION_JSON)
					.body(requestBody)
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
