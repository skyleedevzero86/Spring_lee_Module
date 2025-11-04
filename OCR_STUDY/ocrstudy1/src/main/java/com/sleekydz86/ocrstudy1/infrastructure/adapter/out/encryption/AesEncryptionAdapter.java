package com.sleekydz86.ocrstudy1.infrastructure.adapter.out.encryption;

import com.sleekydz86.ocrstudy1.doamin.service.EncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

@Slf4j
@Component
public class AesEncryptionAdapter implements EncryptionService {

    private final SecretKey secretKey;
    private final String transformation;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    public AesEncryptionAdapter(SecretKey secretKey,
                                @Value("${encryption.transformation}") String transformation) {
        this.secretKey = secretKey;
        this.transformation = transformation;
    }

    @Override
    public String encrypt(String plainText) {
        try {
            if (plainText == null || plainText.isEmpty()) {
                return plainText;
            }

            byte[] encrypted = encryptBytes(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    @Override
    public String decrypt(String encryptedText) {
        try {
            if (encryptedText == null || encryptedText.isEmpty()) {
                return encryptedText;
            }

            byte[] encrypted = Base64.getDecoder().decode(encryptedText);
            byte[] decrypted = decryptBytes(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Decryption failed", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }

    @Override
    public byte[] encryptBytes(byte[] data) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new Random().nextBytes(iv);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            byte[] cipherText = cipher.doFinal(data);

            ByteBuffer byteBuffer = ByteBuffer.allocate(4 + iv.length + cipherText.length);
            byteBuffer.putInt(iv.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return byteBuffer.array();
        } catch (Exception e) {
            log.error("Byte encryption failed", e);
            throw new RuntimeException("Byte encryption failed", e);
        }
    }

    @Override
    public byte[] decryptBytes(byte[] encryptedData) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedData);
            int ivLength = byteBuffer.getInt();

            if (ivLength < 12 || ivLength >= 16) {
                throw new IllegalArgumentException("Invalid IV length");
            }

            byte[] iv = new byte[ivLength];
            byteBuffer.get(iv);
            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            return cipher.doFinal(cipherText);
        } catch (Exception e) {
            log.error("Byte decryption failed", e);
            throw new RuntimeException("Byte decryption failed", e);
        }
    }
}