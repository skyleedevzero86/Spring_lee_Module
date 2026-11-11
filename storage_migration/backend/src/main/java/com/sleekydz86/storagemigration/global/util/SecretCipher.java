package com.sleekydz86.storagemigration.global.util;

import com.sleekydz86.storagemigration.global.config.AppProperties;
import com.sleekydz86.storagemigration.global.error.BusinessException;
import com.sleekydz86.storagemigration.global.error.ErrorCode;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SecretCipher {

    private static final String PREFIX = "enc:v1:";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_BITS = 128;
    private static final String KEY_FILE = "secret.key";

    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    @Autowired
    public SecretCipher(AppProperties properties) {
        this(resolveSecret(properties));
    }

    private SecretCipher(String secret) {
        byte[] digest = Checksums.sha256().digest(secret.getBytes(StandardCharsets.UTF_8));
        this.key = new SecretKeySpec(digest, "AES");
    }

    public static SecretCipher withSecret(String secret) {
        return new SecretCipher(Require.notBlank(secret, "secret"));
    }

    public String encrypt(String plain) {
        if (plain == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] packed = ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array();
            return PREFIX + Base64.getEncoder().encodeToString(packed);
        } catch (GeneralSecurityException exception) {
            throw new BusinessException(ErrorCode.CRYPTO_FAILED, null, exception);
        }
    }

    public String decrypt(String stored) {
        if (stored == null) {
            return null;
        }
        if (!stored.startsWith(PREFIX)) {
            throw new BusinessException(ErrorCode.CRYPTO_FAILED, "암호화되지 않은 값입니다.");
        }
        try {
            byte[] packed = Base64.getDecoder().decode(stored.substring(PREFIX.length()));
            ByteBuffer buffer = ByteBuffer.wrap(packed);
            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.CRYPTO_FAILED, "복호화하지 못했습니다.", exception);
        }
    }

    public boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX);
    }

    private static String resolveSecret(AppProperties properties) {
        if (Texts.hasText(properties.cryptoSecret())) {
            return properties.cryptoSecret();
        }
        Path keyFile = properties.home().resolve(KEY_FILE);
        try {
            if (Files.exists(keyFile)) {
                return Files.readString(keyFile, StandardCharsets.UTF_8).trim();
            }
            Files.createDirectories(properties.home());
            byte[] generated = new byte[32];
            new SecureRandom().nextBytes(generated);
            String secret = Base64.getEncoder().encodeToString(generated);
            Files.writeString(keyFile, secret, StandardCharsets.UTF_8);
            return secret;
        } catch (IOException exception) {
            throw new IllegalStateException("암호화 키 파일을 준비하지 못했습니다: " + keyFile, exception);
        }
    }
}
