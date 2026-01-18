package com.sleekydz86.ftpserver.domain.ftp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FTP 전송 ID 값 객체 단위 테스트")
class FtpTransferIdTest {
    
    private static final Logger log = LoggerFactory.getLogger(FtpTransferIdTest.class);

    @Test
    @DisplayName("FTP 전송 ID를 생성할 수 있다")
    void generateFtpTransferId() {
        // given: ID 생성이 필요할 때
        // when: ID를 생성하면
        FtpTransferId id1 = FtpTransferId.generate();
        FtpTransferId id2 = FtpTransferId.generate();

        // then: 고유한 ID가 생성된다
        assertThat(id1.getValue()).isNotNull();
        assertThat(id2.getValue()).isNotNull();
        assertThat(id1).isNotEqualTo(id2);
        
        log.info("FTP 전송 ID 생성: id1={}, id2={}", id1.getValue(), id2.getValue());
    }

    @Test
    @DisplayName("문자열로부터 FTP 전송 ID를 생성할 수 있다")
    void ofFtpTransferId() {
        // given: 문자열 ID가 주어졌을 때
        String idValue = "test-id-123";

        // when: 문자열로 ID를 생성하면
        FtpTransferId id = FtpTransferId.of(idValue);

        // then: 해당 값을 가진 ID가 생성된다
        assertThat(id.getValue()).isEqualTo(idValue);
        
        log.info("문자열로 ID 생성: id={}", id.getValue());
    }

    @Test
    @DisplayName("null 또는 빈 문자열로 ID를 생성할 수 없다")
    void ofFtpTransferIdWithInvalidValue() {
        // given: null 또는 빈 문자열이 주어졌을 때
        // when & then: ID를 생성하려고 하면 예외가 발생한다
        assertThatThrownBy(() -> FtpTransferId.of(null))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThatThrownBy(() -> FtpTransferId.of(""))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThatThrownBy(() -> FtpTransferId.of("   "))
            .isInstanceOf(IllegalArgumentException.class);
        
        log.warn("잘못된 값으로 ID 생성 시도: null, empty, blank");
    }

    @Test
    @DisplayName("동일한 값을 가진 ID는 같다고 판단된다")
    void equalsFtpTransferId() {
        // given: 동일한 값을 가진 두 ID가 주어졌을 때
        String idValue = "same-id";
        FtpTransferId id1 = FtpTransferId.of(idValue);
        FtpTransferId id2 = FtpTransferId.of(idValue);

        // when: 두 ID를 비교하면
        // then: 같다고 판단된다
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        
        log.info("ID 동등성 확인: id1={}, id2={}, equals={}", 
            id1.getValue(), id2.getValue(), id1.equals(id2));
    }

    @Test
    @DisplayName("다른 값을 가진 ID는 다르다고 판단된다")
    void notEqualsFtpTransferId() {
        // given: 다른 값을 가진 두 ID가 주어졌을 때
        FtpTransferId id1 = FtpTransferId.of("id-1");
        FtpTransferId id2 = FtpTransferId.of("id-2");

        // when: 두 ID를 비교하면
        // then: 다르다고 판단된다
        assertThat(id1).isNotEqualTo(id2);
        
        log.info("ID 비동등성 확인: id1={}, id2={}", id1.getValue(), id2.getValue());
    }
}
