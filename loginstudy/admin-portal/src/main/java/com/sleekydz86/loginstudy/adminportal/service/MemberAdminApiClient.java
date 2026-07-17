package com.sleekydz86.loginstudy.adminportal.service;

import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class MemberAdminApiClient {

	private final RestClient memberApiRestClient;

	public MemberAdminApiClient(RestClient memberApiRestClient) {
		this.memberApiRestClient = memberApiRestClient;
	}

	public ApiCallResult listMembers(String accessToken) {
		return get(accessToken, "/api/admin/members?page=0&size=20");
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

	public record ApiCallResult(boolean success, int statusCode, String body) {

		public static ApiCallResult success(String body) {
			return new ApiCallResult(true, 200, body);
		}

		public static ApiCallResult failure(int statusCode, String body) {
			return new ApiCallResult(false, statusCode, body == null ? "" : body);
		}
	}
}
