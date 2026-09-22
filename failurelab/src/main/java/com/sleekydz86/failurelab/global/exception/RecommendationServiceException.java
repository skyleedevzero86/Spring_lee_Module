package com.sleekydz86.failurelab.global.exception;

public final class RecommendationServiceException extends SimulatedServiceException {

	public RecommendationServiceException() {
		super("추천 서비스 호출에 실패했습니다");
	}
}
