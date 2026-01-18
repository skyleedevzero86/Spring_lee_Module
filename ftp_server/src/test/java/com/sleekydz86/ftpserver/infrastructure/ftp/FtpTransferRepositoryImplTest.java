package com.sleekydz86.ftpserver.infrastructure.ftp;

import com.sleekydz86.ftpserver.domain.ftp.FtpTransfer;
import com.sleekydz86.ftpserver.domain.ftp.FtpTransferId;
import com.sleekydz86.ftpserver.domain.ftp.FtpTransferStatus;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FTP 전송 리포지토리 통합 테스트")
class FtpTransferRepositoryImplTest {
    
    private static final Logger log = LoggerFactory.getLogger(FtpTransferRepositoryImplTest.class);

    @Mock
    private FtpTransferMapper mapper;

    @InjectMocks
    private FtpTransferRepositoryImpl repository;

    @Test
    @DisplayName("FTP 전송을 저장할 수 있다")
    void saveFtpTransfer() {
        // given: 저장할 전송이 주어졌을 때
        FtpTransfer transfer = FtpTransfer.create("PAY-001", "path", "file.txt", "content".getBytes());
        doNothing().when(mapper).processFtpTransfer(any());

        // when: 전송을 저장하면
        FtpTransfer result = repository.save(transfer);

        // then: 전송이 저장되고 반환된다
        assertThat(result).isEqualTo(transfer);
        verify(mapper, times(1)).processFtpTransfer(any());
        
        log.info("FTP 전송 저장 성공: transferId={}", transfer.getTransferId().getValue());
    }

    @Test
    @DisplayName("전송 ID로 전송을 조회할 수 있다")
    void findById() {
        // given: 저장된 전송이 주어졌을 때
        FtpTransferId transferId = FtpTransferId.generate();
        FtpTransfer transfer = FtpTransfer.create("PAY-001", "path", "file.txt", "content".getBytes());
        when(mapper.selectFtpTransfer(transferId.getValue())).thenReturn(transfer);

        // when: 전송 ID로 조회하면
        Optional<FtpTransfer> result = repository.findById(transferId);

        // then: 전송이 반환된다
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(transfer);
        
        log.info("전송 ID로 조회 성공: transferId={}", transferId.getValue());
    }

    @Test
    @DisplayName("존재하지 않는 전송 ID로 조회하면 빈 Optional이 반환된다")
    void findByIdNotFound() {
        // given: 존재하지 않는 전송 ID가 주어졌을 때
        FtpTransferId transferId = FtpTransferId.generate();
        when(mapper.selectFtpTransfer(transferId.getValue())).thenReturn(null);

        // when: 전송 ID로 조회하면
        Optional<FtpTransfer> result = repository.findById(transferId);

        // then: 빈 Optional이 반환된다
        assertThat(result).isEmpty();
        
        log.warn("존재하지 않는 전송 조회: transferId={}", transferId.getValue());
    }

    @Test
    @DisplayName("지출결의번호로 전송 목록을 조회할 수 있다")
    void findByPaymentRequestNo() {
        // given: 동일한 지출결의번호를 가진 전송들이 주어졌을 때
        String paymentRequestNo = "PAY-2024-001";
        FtpTransfer transfer1 = FtpTransfer.create(paymentRequestNo, "path1", "file1.txt", "content1".getBytes());
        FtpTransfer transfer2 = FtpTransfer.create(paymentRequestNo, "path2", "file2.txt", "content2".getBytes());
        List<FtpTransfer> transfers = Arrays.asList(transfer1, transfer2);
        when(mapper.selectFtpTransfersByPaymentRequestNo(paymentRequestNo)).thenReturn(transfers);

        // when: 지출결의번호로 조회하면
        List<FtpTransfer> result = repository.findByPaymentRequestNo(paymentRequestNo);

        // then: 해당 지출결의번호의 모든 전송이 반환된다
        assertThat(result).hasSize(2);
        assertThat(result).extracting(FtpTransfer::getPaymentRequestNo)
            .containsOnly(paymentRequestNo);
        
        log.info("지출결의번호로 조회 성공: paymentRequestNo={}, count={}", 
            paymentRequestNo, result.size());
    }

    @Test
    @DisplayName("상태로 전송 목록을 조회할 수 있다")
    void findByStatus() {
        // given: FAILED 상태의 전송들이 주어졌을 때
        FtpTransfer failed1 = FtpTransfer.create("PAY-001", "path", "file1.txt", "content".getBytes());
        failed1.fail("연결 실패");
        FtpTransfer failed2 = FtpTransfer.create("PAY-002", "path", "file2.txt", "content".getBytes());
        failed2.fail("타임아웃");
        List<FtpTransfer> failedTransfers = Arrays.asList(failed1, failed2);
        when(mapper.selectFtpTransfersByStatus(FtpTransferStatus.FAILED.name())).thenReturn(failedTransfers);

        // when: FAILED 상태로 조회하면
        List<FtpTransfer> result = repository.findByStatus(FtpTransferStatus.FAILED);

        // then: FAILED 상태의 모든 전송이 반환된다
        assertThat(result).hasSize(2);
        assertThat(result).extracting(FtpTransfer::getTransferStatus)
            .containsOnly(FtpTransferStatus.FAILED);
        
        log.info("상태로 조회 성공: status={}, count={}", 
            FtpTransferStatus.FAILED, result.size());
    }

    @Test
    @DisplayName("실패한 전송 목록을 조회할 수 있다")
    void findFailedTransfers() {
        // given: 실패한 전송들이 주어졌을 때
        FtpTransfer failed1 = FtpTransfer.create("PAY-001", "path", "file1.txt", "content".getBytes());
        failed1.fail("연결 실패");
        List<FtpTransfer> failedTransfers = Collections.singletonList(failed1);
        when(mapper.selectFailedFtpTransfers()).thenReturn(failedTransfers);

        // when: 실패한 전송 목록을 조회하면
        List<FtpTransfer> result = repository.findFailedTransfers();

        // then: 실패한 모든 전송이 반환된다
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTransferStatus()).isEqualTo(FtpTransferStatus.FAILED);
        
        log.info("실패한 전송 목록 조회: count={}", result.size());
    }
}
