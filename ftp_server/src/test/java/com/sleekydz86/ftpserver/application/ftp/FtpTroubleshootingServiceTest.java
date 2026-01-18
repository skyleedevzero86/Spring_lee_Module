package com.sleekydz86.ftpserver.application.ftp;

import com.sleekydz86.ftpserver.domain.ftp.FtpTransfer;
import com.sleekydz86.ftpserver.domain.ftp.FtpTransferId;
import com.sleekydz86.ftpserver.domain.ftp.FtpTransferRepository;
import com.sleekydz86.ftpserver.domain.ftp.FtpTransferStatus;
import com.sleekydz86.ftpserver.infrastructure.ftp.FtpClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FTP 트러블슈팅 서비스 단위 테스트")
class FtpTroubleshootingServiceTest {
    
    private static final Logger log = LoggerFactory.getLogger(FtpTroubleshootingServiceTest.class);

    @Mock
    private FtpTransferRepository repository;

    @Mock
    private FtpClient ftpClient;

    @InjectMocks
    private FtpTroubleshootingService troubleshootingService;

    @Test
    @DisplayName("FTP 연결 테스트를 수행할 수 있다")
    void testConnection() {
        // given: FTP 서버가 정상적으로 연결 가능할 때
        when(ftpClient.testConnection()).thenReturn(true);

        // when: 연결 테스트를 수행하면
        boolean result = troubleshootingService.testConnection();

        // then: 연결 성공 결과가 반환된다
        assertThat(result).isTrue();
        verify(ftpClient, times(1)).testConnection();
        
        log.info("FTP 연결 테스트 성공: result={}", result);
    }

    @Test
    @DisplayName("FTP 연결 실패 시 false를 반환한다")
    void testConnectionFailure() {
        // given: FTP 서버 연결이 실패할 때
        when(ftpClient.testConnection()).thenReturn(false);

        // when: 연결 테스트를 수행하면
        boolean result = troubleshootingService.testConnection();

        // then: 연결 실패 결과가 반환된다
        assertThat(result).isFalse();
        
        log.warn("FTP 연결 테스트 실패: result={}", result);
    }

    @Test
    @DisplayName("전송 통계를 조회할 수 있다")
    void getStatistics() {
        // given: 다양한 상태의 전송들이 주어졌을 때
        FtpTransfer success1 = FtpTransfer.create("PAY-001", "path", "file1.txt", "content".getBytes());
        success1.start();
        success1.complete();
        
        FtpTransfer failed1 = FtpTransfer.create("PAY-002", "path", "file2.txt", "content".getBytes());
        failed1.fail("연결 실패");
        
        FtpTransfer pending1 = FtpTransfer.create("PAY-003", "path", "file3.txt", "content".getBytes());
        
        when(repository.findByStatus(FtpTransferStatus.PENDING)).thenReturn(Collections.singletonList(pending1));
        when(repository.findByStatus(FtpTransferStatus.IN_PROGRESS)).thenReturn(Collections.emptyList());
        when(repository.findByStatus(FtpTransferStatus.SUCCESS)).thenReturn(Collections.singletonList(success1));
        when(repository.findByStatus(FtpTransferStatus.FAILED)).thenReturn(Collections.singletonList(failed1));
        when(repository.findByStatus(FtpTransferStatus.RETRYING)).thenReturn(Collections.emptyList());

        // when: 통계를 조회하면
        var statistics = troubleshootingService.getStatistics();

        // then: 각 상태별 통계가 반환된다
        assertThat(statistics.getTotal()).isEqualTo(2);
        assertThat(statistics.getSuccess()).isEqualTo(1);
        assertThat(statistics.getFailed()).isEqualTo(1);
        assertThat(statistics.getPending()).isEqualTo(1);
        assertThat(statistics.getSuccessRate()).isGreaterThan(0);
        
        log.info("전송 통계 조회: total={}, success={}, failed={}, successRate={}%", 
            statistics.getTotal(), statistics.getSuccess(), statistics.getFailed(), 
            statistics.getSuccessRate());
    }

    @Test
    @DisplayName("실패한 전송을 재시도할 수 있다")
    void retryTransfer() {
        // given: 실패한 전송이 주어졌을 때
        FtpTransferId transferId = FtpTransferId.generate();
        FtpTransfer transfer = FtpTransfer.create("PAY-001", "path", "file.txt", "content".getBytes());
        transfer.fail("연결 실패");
        
        when(repository.findById(transferId)).thenReturn(Optional.of(transfer));

        // when: 재시도를 수행하면
        troubleshootingService.retryTransfer(transferId);

        // then: 전송이 재시도 상태로 변경되고 저장된다
        verify(repository, atLeast(1)).save(transfer);
        
        log.info("전송 재시도 수행: transferId={}, retryCount={}", 
            transferId.getValue(), transfer.getRetryCount());
    }

    @Test
    @DisplayName("재시도할 수 없는 전송은 재시도되지 않는다")
    void retryTransferCannotRetry() {
        // given: 재시도할 수 없는 전송이 주어졌을 때
        FtpTransferId transferId = FtpTransferId.generate();
        FtpTransfer transfer = FtpTransfer.create("PAY-001", "path", "file.txt", "content".getBytes());
        transfer.start();
        transfer.complete();
        
        when(repository.findById(transferId)).thenReturn(Optional.of(transfer));

        // when: 재시도를 수행하면
        troubleshootingService.retryTransfer(transferId);

        // then: 재시도가 수행되지 않는다
        verify(repository, never()).save(transfer);
        
        log.warn("재시도 불가능한 전송: transferId={}, status={}", 
            transferId.getValue(), transfer.getTransferStatus());
    }
}
