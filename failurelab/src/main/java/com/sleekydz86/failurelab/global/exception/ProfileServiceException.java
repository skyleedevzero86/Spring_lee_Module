package com.sleekydz86.failurelab.global.exception;

public final class ProfileServiceException extends SimulatedServiceException {

	public ProfileServiceException() {
		super("프로필 서비스 호출에 실패했습니다");
	}
}
