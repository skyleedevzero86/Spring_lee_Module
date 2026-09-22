package com.sleekydz86.failurelab.global.exception;

public final class OrderServiceException extends SimulatedServiceException {

	public OrderServiceException() {
		super("주문 서비스 호출에 실패했습니다");
	}
}
