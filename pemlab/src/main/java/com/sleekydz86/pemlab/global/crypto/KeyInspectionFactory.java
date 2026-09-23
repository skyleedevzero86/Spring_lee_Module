package com.sleekydz86.pemlab.global.crypto;

import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECKey;
import java.security.interfaces.RSAKey;

import com.sleekydz86.pemlab.pem.domain.PemInspection;

public final class KeyInspectionFactory {

	private KeyInspectionFactory() {
	}

	public static PemInspection fromPublicKey(PublicKey publicKey) {
		return new PemInspection(
			"PUBLIC KEY",
			publicKey.getAlgorithm(),
			formatOrUnknown(publicKey),
			sizeLabel(publicKey)
		);
	}

	public static PemInspection fromPrivateKey(PrivateKey privateKey) {
		return new PemInspection(
			"PRIVATE KEY",
			privateKey.getAlgorithm(),
			formatOrUnknown(privateKey),
			sizeLabel(privateKey)
		);
	}

	public static PemInspection fromKeyPair(KeyPair keyPair) {
		return fromPublicKey(keyPair.getPublic());
	}

	private static String formatOrUnknown(Key key) {
		String format = key.getFormat();
		return format == null || format.isBlank() ? "알 수 없음" : format;
	}

	private static String sizeLabel(Key key) {
		return switch (key) {
			case RSAKey rsaKey -> rsaKey.getModulus().bitLength() + " bits";
			case ECKey ecKey -> ecKey.getParams().getOrder().bitLength() + " bits";
			default -> "알 수 없음";
		};
	}
}
