package com.sleekydz86.loginstudy.member.service;

public class AccessDeniedBusinessException extends RuntimeException {

	public AccessDeniedBusinessException(String message) {
		super(message);
	}
}
