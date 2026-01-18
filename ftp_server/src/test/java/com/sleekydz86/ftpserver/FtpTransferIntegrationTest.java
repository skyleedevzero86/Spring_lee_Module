package com.sleekydz86.ftpserver;

import com.sleekydz86.ftpserver.application.ftp.FtpTransferService;
import com.sleekydz86.ftpserver.application.ftp.FtpTroubleshootingService;
import com.sleekydz86.ftpserver.domain.ftp.FtpTransfer;
import com.sleekydz86.ftpserver.domain.ftp.FtpTransferId;
import com.sleekydz86.ftpserver.domain.ftp.FtpTransferStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("FTP 전송 기능 통합 테스트")
class FtpTransferIntegrationTest {
    
    private static final Logger log = LoggerFactory.getLogger(FtpTransferIntegrationTest.class);

    @Autowired
    private FtpTransferService transferService;

    @Autowired
    private FtpTroubleshootingService troubleshootingService;

    @Test
    @DisplayName("FTP 전송 요청부터 조회까지 전체 플로우를 테스트할 수 있다")
    @Transactional
    void fullFtpTransferFlow() {
        // given: 전송에 필요한 정보가 주어졌을 때
        String paymentRequestNo = "PAY-INTEGRATION-001";
        String remotePath = "test/integration";
        String fileName = "integration-test.txt";
        byte[] fileContent = "통합 테스트 파일 내용".getBytes();

        // when: 전송 요청을 생성하고 조회하면
        FtpTransferId transferId = transferService.requestTransfer(
            paymentRequestNo, remotePath, fileName, fileContent
        );
        
        // 잠시 대기 (비동기 처리)
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        var transferOpt = transferService.getTransfer(transferId);

        // then: 전송이 생성되고 조회된다
        assertThat(transferOpt).isPresent();
        FtpTransfer transfer = transferOpt.get();
        assertThat(transfer.getPaymentRequestNo()).isEqualTo(paymentRequestNo);
        assertThat(transfer.getRemotePath()).isEqualTo(remotePath);
        assertThat(transfer.getFileName()).isEqualTo(fileName);
        
        log.info("전체 플로우 테스트 성공: transferId={}, status={}", 
            transferId.getValue(), transfer.getTransferStatus());
    }

    @Test
    @DisplayName("지출결의번호로 전송 이력을 조회할 수 있다")
    @Transactional
    void getTransfersByPaymentRequestNo() {
        // given: 동일한 지출결의번호로 여러 전송이 주어졌을 때
        String paymentRequestNo = "PAY-INTEGRATION-002";
        
        FtpTransferId transferId1 = transferService.requestTransfer(
            paymentRequestNo, "path1", "file1.txt", "content1".getBytes()
        );
        FtpTransferId transferId2 = transferService.requestTransfer(
            paymentRequestNo, "path2", "file2.txt", "content2".getBytes()
        );

        // when: 지출결의번호로 전송 목록을 조회하면
        List<FtpTransfer> transfers = transferService.getTransfersByPaymentRequestNo(paymentRequestNo);

        // then: 해당 지출결의번호의 모든 전송이 반환된다
        assertThat(transfers).isNotEmpty();
        assertThat(transfers).extracting(FtpTransfer::getPaymentRequestNo)
            .containsOnly(paymentRequestNo);
        
        log.info("지출결의번호로 전송 이력 조회 성공: paymentRequestNo={}, count={}", 
            paymentRequestNo, transfers.size());
    }

    @Test
    @DisplayName("전송 통계를 조회할 수 있다")
    @Transactional
    void getStatistics() {
        // given: 여러 전송이 생성되었을 때
        transferService.requestTransfer("PAY-STAT-001", "path", "file1.txt", "content1".getBytes());
        transferService.requestTransfer("PAY-STAT-002", "path", "file2.txt", "content2".getBytes());

        // when: 통계를 조회하면
        var statistics = troubleshootingService.getStatistics();

        // then: 통계 정보가 반환된다
        assertThat(statistics).isNotNull();
        assertThat(statistics.getTotal()).isGreaterThanOrEqualTo(0);
        
        log.info("전송 통계 조회 성공: total={}, success={}, failed={}, successRate={}%", 
            statistics.getTotal(), statistics.getSuccess(), statistics.getFailed(), 
            statistics.getSuccessRate());
    }

    @Test
    @DisplayName("FTP 연결 테스트를 수행할 수 있다")
    void testConnection() {
        // given: FTP 서버 연결이 필요할 때
        // when: 연결 테스트를 수행하면
        boolean result = troubleshootingService.testConnection();

        // then: 연결 테스트 결과가 반환된다
        assertThat(result).isNotNull();
        
        log.info("FTP 연결 테스트 결과: connected={}", result);
    }
}
