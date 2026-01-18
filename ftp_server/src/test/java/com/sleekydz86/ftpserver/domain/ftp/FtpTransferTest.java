package com.sleekydz86.ftpserver.domain.ftp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FTP 전송 도메인 엔티티 단위 테스트")
class FtpTransferTest {
    
    private static final Logger log = LoggerFactory.getLogger(FtpTransferTest.class);

    @Test
    @DisplayName("FTP 전송 요청을 생성할 수 있다")
    void createFtpTransfer() {
        // given: 전송에 필요한 정보가 주어졌을 때
        String paymentRequestNo = "PAY-2024-001";
        String remotePath = "project/2024";
        String fileName = "test.pdf";
        byte[] fileContent = "테스트 파일 내용".getBytes();

        // when: FTP 전송을 생성하면
        FtpTransfer transfer = FtpTransfer.create(paymentRequestNo, remotePath, fileName, fileContent);

        // then: 전송이 PENDING 상태로 생성되고 이벤트가 기록된다
        assertThat(transfer.getPaymentRequestNo()).isEqualTo(paymentRequestNo);
        assertThat(transfer.getRemotePath()).isEqualTo(remotePath);
        assertThat(transfer.getFileName()).isEqualTo(fileName);
        assertThat(transfer.getFileContent()).isEqualTo(fileContent);
        assertThat(transfer.getTransferStatus()).isEqualTo(FtpTransferStatus.PENDING);
        assertThat(transfer.getRetryCount()).isZero();
        assertThat(transfer.getMaxRetries()).isEqualTo(3);
        assertThat(transfer.getCreatedAt()).isNotNull();
        assertThat(transfer.getEvents()).hasSize(1);
        
        log.info("FTP 전송 생성 성공: transferId={}, status={}", 
            transfer.getTransferId().getValue(), transfer.getTransferStatus());
    }

    @Test
    @DisplayName("FTP 전송을 시작할 수 있다")
    void startFtpTransfer() {
        // given: PENDING 상태의 전송이 주어졌을 때
        FtpTransfer transfer = FtpTransfer.create("PAY-001", "path", "file.txt", "content".getBytes());

        // when: 전송을 시작하면
        transfer.start();

        // then: 상태가 IN_PROGRESS로 변경되고 시작 시간이 기록된다
        assertThat(transfer.getTransferStatus()).isEqualTo(FtpTransferStatus.IN_PROGRESS);
        assertThat(transfer.getStartedAt()).isNotNull();
        assertThat(transfer.getEvents()).hasSize(2);
        
        log.info("FTP 전송 시작 성공: status={}, startedAt={}", 
            transfer.getTransferStatus(), transfer.getStartedAt());
    }

    @Test
    @DisplayName("RETRYING 상태에서도 전송을 시작할 수 있다")
    void startFtpTransferFromRetrying() {
        // given: RETRYING 상태의 전송이 주어졌을 때
        FtpTransfer transfer = FtpTransfer.create("PAY-001", "path", "file.txt", "content".getBytes());
        transfer.fail("임시 실패");
        transfer.retry();

        // when: 전송을 시작하면
        transfer.start();

        // then: 상태가 IN_PROGRESS로 변경된다
        assertThat(transfer.getTransferStatus()).isEqualTo(FtpTransferStatus.IN_PROGRESS);
        
        log.info("재시도 전송 시작 성공: status={}", transfer.getTransferStatus());
    }

    @Test
    @DisplayName("IN_PROGRESS 상태가 아닌 경우 전송을 완료할 수 없다")
    void completeFtpTransferWithInvalidStatus() {
        // given: PENDING 상태의 전송이 주어졌을 때
        FtpTransfer transfer = FtpTransfer.create("PAY-001", "path", "file.txt", "content".getBytes());

        // when & then: 전송을 완료하려고 하면 예외가 발생한다
        assertThatThrownBy(() -> transfer.complete())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("완료할 수 없는 상태");
        
        log.warn("잘못된 상태에서 완료 시도: status={}", transfer.getTransferStatus());
    }

    @Test
    @DisplayName("FTP 전송을 성공적으로 완료할 수 있다")
    void completeFtpTransfer() {
        // given: IN_PROGRESS 상태의 전송이 주어졌을 때
        FtpTransfer transfer = FtpTransfer.create("PAY-001", "path", "file.txt", "content".getBytes());
        transfer.start();

        // when: 전송을 완료하면
        transfer.complete();

        // then: 상태가 SUCCESS로 변경되고 완료 시간이 기록된다
        assertThat(transfer.getTransferStatus()).isEqualTo(FtpTransferStatus.SUCCESS);
        assertThat(transfer.getCompletedAt()).isNotNull();
        assertThat(transfer.getErrorMessage()).isNull();
        assertThat(transfer.getEvents()).hasSize(3);
        
        log.info("FTP 전송 완료 성공: status={}, completedAt={}", 
            transfer.getTransferStatus(), transfer.getCompletedAt());
    }

    @Test
    @DisplayName("FTP 전송 실패를 처리할 수 있다")
    void failFtpTransfer() {
        // given: IN_PROGRESS 상태의 전송이 주어졌을 때
        FtpTransfer transfer = FtpTransfer.create("PAY-001", "path", "file.txt", "content".getBytes());
        transfer.start();
        String errorMessage = "연결 실패";

        // when: 전송을 실패 처리하면
        transfer.fail(errorMessage);

        // then: 상태가 FAILED로 변경되고 에러 메시지가 기록된다
        assertThat(transfer.getTransferStatus()).isEqualTo(FtpTransferStatus.FAILED);
        assertThat(transfer.getErrorMessage()).isEqualTo(errorMessage);
        assertThat(transfer.getCompletedAt()).isNotNull();
        
        log.error("FTP 전송 실패 처리: status={}, error={}", 
            transfer.getTransferStatus(), transfer.getErrorMessage());
    }

    @Test
    @DisplayName("실패한 전송을 재시도할 수 있다")
    void retryFtpTransfer() {
        // given: FAILED 상태의 전송이 주어졌을 때
        FtpTransfer transfer = FtpTransfer.create("PAY-001", "path", "file.txt", "content".getBytes());
        transfer.fail("연결 실패");
        int initialRetryCount = transfer.getRetryCount();

        // when: 전송을 재시도하면
        transfer.retry();

        // then: 상태가 RETRYING으로 변경되고 재시도 횟수가 증가한다
        assertThat(transfer.getTransferStatus()).isEqualTo(FtpTransferStatus.RETRYING);
        assertThat(transfer.getRetryCount()).isEqualTo(initialRetryCount + 1);
        assertThat(transfer.getErrorMessage()).isNull();
        
        log.info("FTP 전송 재시도: retryCount={}/{}", 
            transfer.getRetryCount(), transfer.getMaxRetries());
    }

    @Test
    @DisplayName("최대 재시도 횟수를 초과하면 재시도할 수 없다")
    void retryFtpTransferExceedsMaxRetries() {
        // given: 최대 재시도 횟수에 도달한 전송이 주어졌을 때
        FtpTransfer transfer = FtpTransfer.create("PAY-001", "path", "file.txt", "content".getBytes());
        transfer.fail("실패1");
        transfer.retry();
        transfer.fail("실패2");
        transfer.retry();
        transfer.fail("실패3");
        transfer.retry();
        transfer.fail("실패4");

        // when & then: 재시도하려고 하면 예외가 발생한다
        assertThatThrownBy(() -> transfer.retry())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("최대 재시도 횟수");
        
        log.warn("최대 재시도 횟수 초과: retryCount={}, maxRetries={}", 
            transfer.getRetryCount(), transfer.getMaxRetries());
    }

    @Test
    @DisplayName("전송을 취소할 수 있다")
    void cancelFtpTransfer() {
        // given: PENDING 상태의 전송이 주어졌을 때
        FtpTransfer transfer = FtpTransfer.create("PAY-001", "path", "file.txt", "content".getBytes());

        // when: 전송을 취소하면
        transfer.cancel();

        // then: 상태가 CANCELLED로 변경되고 완료 시간이 기록된다
        assertThat(transfer.getTransferStatus()).isEqualTo(FtpTransferStatus.CANCELLED);
        assertThat(transfer.getCompletedAt()).isNotNull();
        
        log.info("FTP 전송 취소: status={}", transfer.getTransferStatus());
    }

    @Test
    @DisplayName("이미 종료된 전송은 취소할 수 없다")
    void cancelTerminatedFtpTransfer() {
        // given: SUCCESS 상태로 완료된 전송이 주어졌을 때
        FtpTransfer transfer = FtpTransfer.create("PAY-001", "path", "file.txt", "content".getBytes());
        transfer.start();
        transfer.complete();

        // when & then: 취소하려고 하면 예외가 발생한다
        assertThatThrownBy(() -> transfer.cancel())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("이미 종료된 전송");
        
        log.warn("종료된 전송 취소 시도: status={}", transfer.getTransferStatus());
    }

    @Test
    @DisplayName("재시도 가능 여부를 확인할 수 있다")
    void canRetryFtpTransfer() {
        // given: FAILED 상태의 전송이 주어졌을 때
        FtpTransfer transfer = FtpTransfer.create("PAY-001", "path", "file.txt", "content".getBytes());
        transfer.fail("연결 실패");

        // when: 재시도 가능 여부를 확인하면
        boolean canRetry = transfer.canRetry();

        // then: 재시도 가능하다고 판단된다
        assertThat(canRetry).isTrue();
        
        log.info("재시도 가능 여부 확인: canRetry={}, retryCount={}/{}", 
            canRetry, transfer.getRetryCount(), transfer.getMaxRetries());
    }

    @Test
    @DisplayName("이벤트 목록을 조회할 수 있다")
    void getEvents() {
        // given: 여러 상태 변경을 거친 전송이 주어졌을 때
        FtpTransfer transfer = FtpTransfer.create("PAY-001", "path", "file.txt", "content".getBytes());
        transfer.start();
        transfer.complete();

        // when: 이벤트 목록을 조회하면
        var events = transfer.getEvents();

        // then: 모든 상태 변경 이벤트가 기록되어 있다
        assertThat(events).hasSize(3);
        assertThat(events.get(0).getEventType()).isEqualTo("FTP_TRANSFER_CREATED");
        assertThat(events.get(1).getEventType()).isEqualTo("FTP_TRANSFER_STARTED");
        assertThat(events.get(2).getEventType()).isEqualTo("FTP_TRANSFER_COMPLETED");
        
        log.info("이벤트 목록 조회: eventCount={}", events.size());
    }
}
