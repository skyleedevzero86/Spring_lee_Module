package com.sleekydz86.concurrentlab.customer.domain;

public record CustomerProfile(CustomerId customerId, String name, String grade) {

	public CustomerProfile {
		if (customerId == null) {
			throw new IllegalArgumentException("고객 번호가 필요합니다");
		}
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("고객 이름이 필요합니다");
		}
		if (grade == null || grade.isBlank()) {
			throw new IllegalArgumentException("고객 등급이 필요합니다");
		}
	}
}
