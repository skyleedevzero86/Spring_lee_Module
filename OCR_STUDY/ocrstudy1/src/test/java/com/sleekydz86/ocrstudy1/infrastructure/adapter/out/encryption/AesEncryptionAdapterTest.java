package com.sleekydz86.ocrstudy1.infrastructure.adapter.out.encryption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("AesEncryptionAdapter 테스트")
class AesEncryptionAdapterTest {

    private AesEncryptionAdapter encryptionAdapter;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        byte[] keyBytes = "MySecretKey123456789012345678901234567890".getBytes();
        secretKey = new SecretKeySpec(keyBytes, "AES");
        encryptionAdapter = new AesEncryptionAdapter(secretKey, "AES/GCM/NoPadding");
    }

    @Test
    @DisplayName("문자열 암호화 및 복호화 성공")
    void encryptDecrypt_Success() {
        String plainText = "민감한 정보 텍스트";

        String encrypted = encryptionAdapter.encrypt(plainText);
        String decrypted = encryptionAdapter.decrypt(encrypted);

        assertThat(encrypted).isNotBlank();
        assertThat(encrypted).isNotEqualTo(plainText);
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    @DisplayName("빈 문자열 암호화 및 복호화")
    void encryptDecrypt_EmptyString() {
        String plainText = "";

        String encrypted = encryptionAdapter.encrypt(plainText);
        String decrypted = encryptionAdapter.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    @DisplayName("null 문자열 처리")
    void encryptDecrypt_NullString() {
        String encrypted = encryptionAdapter.encrypt(null);
        String decrypted = encryptionAdapter.decrypt(null);

        assertThat(encrypted).isNull();
        assertThat(decrypted).isNull();
    }

    @Test
    @DisplayName("바이트 배열 암호화 및 복호화 성공")
    void encryptDecryptBytes_Success() {
        byte[] plainBytes = "테스트 바이트 데이터".getBytes();

        byte[] encrypted = encryptionAdapter.encryptBytes(plainBytes);
        byte[] decrypted = encryptionAdapter.decryptBytes(encrypted);

        assertThat(encrypted).isNotNull();
        assertThat(encrypted.length).isGreaterThan(plainBytes.length);
        assertThat(decrypted).isEqualTo(plainBytes);
    }

    @Test
    @DisplayName("같은 텍스트도 다른 암호화 결과 생성 (IV 랜덤)")
    void encrypt_SameText_DifferentResults() {
        String plainText = "같은 텍스트";

        String encrypted1 = encryptionAdapter.encrypt(plainText);
        String encrypted2 = encryptionAdapter.encrypt(plainText);

        assertThat(encrypted1).isNotEqualTo(encrypted2);
        assertThat(encryptionAdapter.decrypt(encrypted1)).isEqualTo(plainText);
        assertThat(encryptionAdapter.decrypt(encrypted2)).isEqualTo(plainText);
    }

    @Test
    @DisplayName("한글 텍스트 암호화 및 복호화")
    void encryptDecrypt_KoreanText() {
        String koreanText = "안녕하세요. 이것은 테스트입니다. 12345 !@#$%";

        String encrypted = encryptionAdapter.encrypt(koreanText);
        String decrypted = encryptionAdapter.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(koreanText);
    }

    @Test
    @DisplayName("긴 텍스트 암호화 및 복호화")
    void encryptDecrypt_LongText() {
        String longText = "A".repeat(1000);

        String encrypted = encryptionAdapter.encrypt(longText);
        String decrypted = encryptionAdapter.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(longText);
    }
}

