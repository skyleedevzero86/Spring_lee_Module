package com.sleekydz86.storagemigration.global.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sleekydz86.storagemigration.global.error.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("입력 정보 암호화")
class SecretCipherTest {

    private final SecretCipher cipher = SecretCipher.withSecret("test-secret");

    @Test
    @DisplayName("암호화한 값에는 원문이 남지 않고, 복호화하면 원문이 나온다")
    void encryptsAndDecryptsWithoutKeepingPlainText() {
        // given
        String plain = "minio-password";

        // when
        String encrypted = cipher.encrypt(plain);

        // then
        assertThat(encrypted).startsWith("enc:v1:").doesNotContain(plain);
        assertThat(cipher.decrypt(encrypted)).isEqualTo(plain);
    }

    @Test
    @DisplayName("같은 값도 암호화할 때마다 결과가 다르다")
    void sameValueEncryptsDifferentlyEachTime() {
        // given
        String plain = "same";

        // when
        String first = cipher.encrypt(plain);
        String second = cipher.encrypt(plain);

        // then
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("다른 키로 암호화한 값은 복호화하지 못한다")
    void rejectsValueEncryptedWithAnotherKey() {
        // given
        String encrypted = SecretCipher.withSecret("other-secret").encrypt("value");

        // when / then
        assertThatThrownBy(() -> cipher.decrypt(encrypted)).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("암호화되지 않은 값은 거부한다")
    void rejectsPlainText() {
        // given
        String plain = "plain";

        // when / then
        assertThatThrownBy(() -> cipher.decrypt(plain)).isInstanceOf(BusinessException.class);
    }
}
