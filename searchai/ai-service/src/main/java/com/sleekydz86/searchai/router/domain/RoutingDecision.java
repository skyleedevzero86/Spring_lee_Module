package com.sleekydz86.searchai.router.domain;

import java.util.Map;
import java.util.UUID;

public record RoutingDecision(
	ModelTier tier,
	String model,
	double confidence,
	Map<String, Double> probabilities,
	RoutingType routingType,
	String requestId
) {

	public RoutingDecision {
		probabilities = probabilities == null ? Map.of() : Map.copyOf(probabilities);
		routingType = routingType == null ? RoutingType.JEV : routingType;
		requestId = requestId == null || requestId.isBlank() ? UUID.randomUUID().toString() : requestId;
	}

	public static RoutingDecision of(
		ModelTier tier,
		String model,
		double confidence,
		Map<String, Double> probabilities,
		RoutingType routingType
	) {
		return new RoutingDecision(tier, model, confidence, probabilities, routingType, UUID.randomUUID().toString());
	}

	public RoutingDecision withTier(ModelTier next, String modelName) {
		return new RoutingDecision(next, modelName, confidence, probabilities, routingType, requestId);
	}

	public RoutingDecision asFallback(ModelTier next, String modelName) {
		return new RoutingDecision(next, modelName, confidence, probabilities, RoutingType.FALLBACK, requestId);
	}

	public String asSsePayload() {
		return asSsePayload(Map.of());
	}

	public String asSsePayload(Map<String, String> extras) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\"tier\":\"").append(tier.displayName())
			.append("\",\"model\":\"").append(model)
			.append("\",\"confidence\":").append(confidence)
			.append(",\"routingType\":\"").append(routingType.name())
			.append("\",\"requestId\":\"").append(requestId).append('"');
		extras.forEach((k, v) -> sb.append(",\"").append(k).append("\":\"").append(v).append('"'));
		sb.append('}');
		return sb.toString();
	}
}
