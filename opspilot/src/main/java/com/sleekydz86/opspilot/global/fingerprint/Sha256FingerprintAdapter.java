package com.sleekydz86.opspilot.global.fingerprint;

import com.sleekydz86.opspilot.incident.application.port.out.FingerprintPort;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public final class Sha256FingerprintAdapter implements FingerprintPort {

	@Override
	public String fingerprint(String serviceName, String exceptionType, String message, String stackTrace) {
		String normalized = String.join("|",
			normalize(serviceName),
			normalize(exceptionType),
			normalize(message),
			firstFrames(stackTrace, 3)
		);
		return sha256(normalized);
	}

	private static String normalize(String value) {
		return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
	}

	private static String firstFrames(String stackTrace, int frames) {
		if (stackTrace == null || stackTrace.isBlank()) {
			return "";
		}
		return stackTrace.lines()
			.map(String::trim)
			.filter(line -> !line.isEmpty())
			.limit(frames)
			.reduce((a, b) -> a + ";" + b)
			.orElse("");
	}

	private static String sha256(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		}
		catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256을 사용할 수 없습니다", ex);
		}
	}
}
