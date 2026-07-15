package com.sleekydz86.loginstudy.userportal.service;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class MemberApiClient {

	private final RestClient memberApiRestClient;

	public MemberApiClient(RestClient memberApiRestClient) {
		this.memberApiRestClient = memberApiRestClient;
	}

	public MemberApiCallResult fetchMyProfile(String accessToken) {
		try {
			String body = memberApiRestClient.get()
					.uri("/api/members/me")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
					.header("X-Correlation-Id", java.util.UUID.randomUUID().toString())
					.retrieve()
					.body(String.class);
			return MemberApiCallResult.success(body == null ? "" : body);
		}
		catch (RestClientResponseException ex) {
			return MemberApiCallResult.failure(
					ex.getStatusCode().value(),
					ex.getResponseBodyAsString());
		}
		catch (Exception ex) {
			return MemberApiCallResult.failure(0, ex.getMessage());
		}
	}

	public record MemberApiCallResult(boolean success, int statusCode, String body) {

		public static MemberApiCallResult success(String body) {
			return new MemberApiCallResult(true, 200, body);
		}

		public static MemberApiCallResult failure(int statusCode, String body) {
			return new MemberApiCallResult(false, statusCode, body == null ? "" : body);
		}
	}
}
