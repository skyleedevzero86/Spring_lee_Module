package com.sleekydz86.loginstudy.member.security;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SensitiveDataCipher {

	private static final String PREFIX = "enc:v1:";
	private static final String TRANSFORMATION = "AES/GCM/NoPadding";
	private static final int NONCE_LENGTH = 12;
	private static final int TAG_LENGTH_BITS = 128;

	private final SecretKey secretKey;
	private final SecureRandom secureRandom = new SecureRandom();

	public SensitiveDataCipher(@Value("${member.encryption.key-base64}") String keyBase64) {
		byte[] key = Base64.getDecoder().decode(keyBase64);
		if (key.length != 32) {
			throw new IllegalArgumentException("member.encryption.key-base64 must decode to exactly 32 bytes");
		}
		this.secretKey = new SecretKeySpec(key, "AES");
	}

	public String encrypt(String plaintext) {
		if (plaintext == null) {
			return plaintext;
		}
		try {
			byte[] nonce = new byte[NONCE_LENGTH];
			secureRandom.nextBytes(nonce);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
			byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
			byte[] payload = ByteBuffer.allocate(nonce.length + encrypted.length)
					.put(nonce)
					.put(encrypted)
					.array();
			return PREFIX + Base64.getEncoder().encodeToString(payload);
		}
		catch (GeneralSecurityException ex) {
			throw new IllegalStateException("민감정보 암호화에 실패했습니다", ex);
		}
	}

	public String decrypt(String storedValue) {
		if (storedValue == null || !storedValue.startsWith(PREFIX)) {
			return storedValue;
		}
		try {
			byte[] payload = Base64.getDecoder().decode(storedValue.substring(PREFIX.length()));
			if (payload.length <= NONCE_LENGTH) {
				throw new IllegalArgumentException("Invalid encrypted payload");
			}
			ByteBuffer buffer = ByteBuffer.wrap(payload);
			byte[] nonce = new byte[NONCE_LENGTH];
			buffer.get(nonce);
			byte[] encrypted = new byte[buffer.remaining()];
			buffer.get(encrypted);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
			return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
		}
		catch (GeneralSecurityException | IllegalArgumentException ex) {
			throw new IllegalStateException("민감정보 복호화에 실패했습니다", ex);
		}
	}

	public boolean isEncrypted(String value) {
		return value != null && value.startsWith(PREFIX);
	}
}
