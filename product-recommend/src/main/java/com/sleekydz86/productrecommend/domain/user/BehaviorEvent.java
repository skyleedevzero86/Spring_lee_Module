package com.sleekydz86.productrecommend.domain.user;

import java.time.Instant;
import java.util.UUID;

public record BehaviorEvent(
	String eventId,
	String impressionId,
	String userId,
	Long productId,
	BehaviorEventType eventType,
	Integer position,
	String query,
	Instant occurredAt
) {
	public static BehaviorEvent of(
		String userId,
		Long productId,
		BehaviorEventType type,
		String impressionId,
		Integer position,
		String query
	) {
		return new BehaviorEvent(
			UUID.randomUUID().toString(),
			impressionId,
			userId,
			productId,
			type,
			position,
			query,
			Instant.now()
		);
	}
}
