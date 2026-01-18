package com.sleekydz86.ftpserver.application.ftp;

import com.sleekydz86.ftpserver.domain.ftp.FtpTransfer;
import com.sleekydz86.ftpserver.domain.ftp.FtpTransferId;
import com.sleekydz86.ftpserver.domain.ftp.FtpTransferRepository;
import com.sleekydz86.ftpserver.domain.ftp.FtpTransferStatus;
import com.sleekydz86.ftpserver.infrastructure.ftp.FtpClient;
import com.sleekydz86.ftpserver.infrastructure.ftp.FtpException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FTP 전송 서비스 단위 테스트")
class FtpTransferServiceTest {
    
    private static final Logger log = LoggerFactory.getLogger(FtpTransferServiceTest.class);

    @Mock
    private FtpTransferRepository repository;

    @Mock
    private FtpClient ftpClient;

    @InjectMocks
    private FtpTransferService transferService;

    @Test
    @DisplayName("FTP 전송 요청을 생성할 수 있다")
    void requestTransfer() {
        // given: 전송에 필요한 정보가 주어졌을 때
        String paymentRequestNo = "PAY-2024-001";
        String remotePath = "project/2024";
        String fileName = "test.pdf";
        byte[] fileContent = "테스트 파일".getBytes();

        // when: 전송 요청을 생성하면
        FtpTransferId transferId = transferService.requestTransfer(
            paymentRequestNo, remotePath, fileName, fileContent
        );

        // then: 전송이 생성되고 저장된다
        assertThat(transferId).isNotNull();
        verify(repository, times(1)).save(any(FtpTransfer.class));
        
        log.info("FTP 전송 요청 생성 성공: transferId={}", transferId.getValue());
    }

    @Test
    @DisplayName("전송 ID로 전송 정보를 조회할 수 있다")
    void getTransfer() {
        // given: 저장된 전송이 주어졌을 때
        FtpTransferId transferId = FtpTransferId.generate();
        FtpTransfer transfer = FtpTransfer.create("PAY-001", "path", "file.txt", "content".getBytes());
        when(repository.findById(transferId)).thenReturn(Optional.of(transfer));

        // when: 전송 정보를 조회하면
        Optional<FtpTransfer> result = transferService.getTransfer(transferId);

        // then: 전송 정보가 반환된다
        assertThat(result).isPresent();
        assertThat(result.get().getTransferId()).isEqualTo(transfer.getTransferId());
        
        log.info("전송 정보 조회 성공: transferId={}, status={}", 
            transferId.getValue(), result.get().getTransferStatus());
    }

    @Test
    @DisplayName("존재하지 않는 전송 ID로 조회하면 빈 Optional이 반환된다")
    void getTransferNotFound() {
        // given: 존재하지 않는 전송 ID가 주어졌을 때
        FtpTransferId transferId = FtpTransferId.generate();
        when(repository.findById(transferId)).thenReturn(Optional.empty());

        // when: 전송 정보를 조회하면
        Optional<FtpTransfer> result = transferService.getTransfer(transferId);

        // then: 빈 Optional이 반환된다
        assertThat(result).isEmpty();
        
        log.warn("존재하지 않는 전송 조회: transferId={}", transferId.getValue());
    }

    @Test
    @DisplayName("지출결의번호로 전송 목록을 조회할 수 있다")
    void getTransfersByPaymentRequestNo() {
        // given: 동일한 지출결의번호를 가진 여러 전송이 주어졌을 때
        String paymentRequestNo = "PAY-2024-001";
        FtpTransfer transfer1 = FtpTransfer.create(paymentRequestNo, "path1", "file1.txt", "content1".getBytes());
        FtpTransfer transfer2 = FtpTransfer.create(paymentRequestNo, "path2", "file2.txt", "content2".getBytes());
        List<FtpTransfer> transfers = Arrays.asList(transfer1, transfer2);
        when(repository.findByPaymentRequestNo(paymentRequestNo)).thenReturn(transfers);

        // when: 지출결의번호로 전송 목록을 조회하면
        List<FtpTransfer> result = transferService.getTransfersByPaymentRequestNo(paymentRequestNo);

        // then: 해당 지출결의번호의 모든 전송이 반환된다
        assertThat(result).hasSize(2);
        assertThat(result).extracting(FtpTransfer::getPaymentRequestNo)
            .containsOnly(paymentRequestNo);
        
        log.info("지출결의번호로 전송 목록 조회: paymentRequestNo={}, count={}", 
            paymentRequestNo, result.size());
    }

    @Test
    @DisplayName("FTP 전송이 성공적으로 완료된다")
    void transferAsyncSuccess() throws FtpException {
        // given: 저장된 전송이 주어졌을 때
        FtpTransferId transferId = FtpTransferId.generate();
        FtpTransfer transfer = FtpTransfer.create("PAY-001", "path", "file.txt", "content".getBytes());
        when(repository.findById(transferId)).thenReturn(Optional.of(transfer));
        doNothing().when(ftpClient).uploadFile(anyString(), anyString(), any(byte[].class));

        // when: 비동기 전송을 실행하면
        transferService.transferAsync(transferId);

        // then: 전송이 완료되고 저장된다
        verify(ftpClient, times(1)).uploadFile(anyString(), anyString(), any(byte[].class));
        verify(repository, atLeast(2)).save(transfer);
        
        log.info("FTP 전송 성공: transferId={}", transferId.getValue());
    }

    @Test
    @DisplayName("FTP 전송 실패 시 실패 상태로 저장된다")
    void transferAsyncFailure() throws FtpException {
        // given: 저장된 전송과 FTP 전송 실패가 주어졌을 때
        FtpTransferId transferId = FtpTransferId.generate();
        FtpTransfer transfer = FtpTransfer.create("PAY-001", "path", "file.txt", "content".getBytes());
        when(repository.findById(transferId)).thenReturn(Optional.of(transfer));
        doThrow(new FtpException("연결 실패")).when(ftpClient).uploadFile(anyString(), anyString(), any(byte[].class));

        // when: 비동기 전송을 실행하면
        transferService.transferAsync(transferId);

        // then: 전송이 실패 상태로 저장된다
        verify(ftpClient, times(1)).uploadFile(anyString(), anyString(), any(byte[].class));
        verify(repository, atLeast(2)).save(transfer);
        
        log.error("FTP 전송 실패 처리: transferId={}", transferId.getValue());
    }
}
