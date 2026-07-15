package com.sleekydz86.loginstudy.member.service;

public class OptimisticLockConflictException extends RuntimeException {

	public OptimisticLockConflictException(String message) {
		super(message);
	}
}
