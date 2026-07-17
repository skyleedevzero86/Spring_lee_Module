package com.sleekydz86.loginstudy.userportal.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class MemberApiClient {

	private final RestClient memberApiRestClient;

	public MemberApiClient(@Qualifier("memberApiRestClient") RestClient memberApiRestClient) {
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

	public MemberApiCallResult updateProfile(
			String accessToken,
			long memberId,
			String requestBody) {
		try {
			String body = memberApiRestClient.patch()
					.uri("/api/members/{id}", memberId)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
					.header("X-Correlation-Id", java.util.UUID.randomUUID().toString())
					.contentType(MediaType.APPLICATION_JSON)
					.body(requestBody)
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
