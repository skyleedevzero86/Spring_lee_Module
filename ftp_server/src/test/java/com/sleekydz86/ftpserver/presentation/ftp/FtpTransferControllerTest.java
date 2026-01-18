package com.sleekydz86.ftpserver.presentation.ftp;

import com.sleekydz86.ftpserver.application.ftp.FtpTransferCommand;
import com.sleekydz86.ftpserver.application.ftp.FtpTransferService;
import com.sleekydz86.ftpserver.application.ftp.FtpTroubleshootingService;
import com.sleekydz86.ftpserver.domain.ftp.FtpTransfer;
import com.sleekydz86.ftpserver.domain.ftp.FtpTransferId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FTP 전송 컨트롤러 통합 테스트")
class FtpTransferControllerTest {
    
    private static final Logger log = LoggerFactory.getLogger(FtpTransferControllerTest.class);

    @Mock
    private FtpTransferService transferService;

    @Mock
    private FtpTroubleshootingService troubleshootingService;

    @InjectMocks
    private FtpTransferController controller;

    private MockMvc mockMvc;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("메인 페이지를 조회할 수 있다")
    void index() throws Exception {
        // given: 통계 정보가 주어졌을 때
        var statistics = new FtpTroubleshootingService.FtpTransferStatistics(10, 8, 2, 0, 0, 0);
        when(troubleshootingService.getStatistics()).thenReturn(statistics);

        // when: 메인 페이지를 요청하면
        // then: 메인 페이지가 반환된다
        mockMvc.perform(get("/ftp"))
            .andExpect(status().isOk())
            .andExpect(view().name("ftp/index"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.model().attributeExists("command"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.model().attributeExists("statistics"));
        
        log.info("메인 페이지 조회 성공");
    }

    @Test
    @DisplayName("FTP 전송 요청을 처리할 수 있다")
    void requestTransfer() throws Exception {
        // given: 전송 요청 정보가 주어졌을 때
        FtpTransferId transferId = FtpTransferId.generate();
        when(transferService.requestTransfer(anyString(), anyString(), anyString(), any(byte[].class)))
            .thenReturn(transferId);
        when(troubleshootingService.getStatistics())
            .thenReturn(new FtpTroubleshootingService.FtpTransferStatistics(0, 0, 0, 0, 0, 0));

        // when: 전송 요청을 보내면
        // then: 전송이 생성되고 메인 페이지로 리다이렉트된다
        mockMvc.perform(post("/ftp/transfer")
                .param("paymentRequestNo", "PAY-2024-001")
                .param("remotePath", "project/2024")
                .param("fileName", "test.pdf")
                .param("fileContent", "테스트 파일 내용"))
            .andExpect(status().isOk())
            .andExpect(view().name("ftp/index"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.model().attributeExists("transferId"));
        
        log.info("FTP 전송 요청 처리 성공: transferId={}", transferId.getValue());
    }

    @Test
    @DisplayName("전송 상세 정보를 조회할 수 있다")
    void getTransfer() throws Exception {
        // given: 저장된 전송이 주어졌을 때
        FtpTransferId transferId = FtpTransferId.generate();
        FtpTransfer transfer = FtpTransfer.create("PAY-001", "path", "file.txt", "content".getBytes());
        when(transferService.getTransfer(transferId)).thenReturn(Optional.of(transfer));

        // when: 전송 상세 정보를 요청하면
        // then: 전송 상세 페이지가 반환된다
        mockMvc.perform(get("/ftp/transfer/{transferId}", transferId.getValue()))
            .andExpect(status().isOk())
            .andExpect(view().name("ftp/transfer-detail"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.model().attributeExists("transfer"));
        
        log.info("전송 상세 정보 조회 성공: transferId={}", transferId.getValue());
    }

    @Test
    @DisplayName("존재하지 않는 전송 조회 시 에러 메시지를 표시한다")
    void getTransferNotFound() throws Exception {
        // given: 존재하지 않는 전송 ID가 주어졌을 때
        FtpTransferId transferId = FtpTransferId.generate();
        when(transferService.getTransfer(transferId)).thenReturn(Optional.empty());

        // when: 전송 상세 정보를 요청하면
        // then: 에러 메시지가 포함된 페이지가 반환된다
        mockMvc.perform(get("/ftp/transfer/{transferId}", transferId.getValue()))
            .andExpect(status().isOk())
            .andExpect(view().name("ftp/transfer-detail"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.model().attributeExists("error"));
        
        log.warn("존재하지 않는 전송 조회: transferId={}", transferId.getValue());
    }

    @Test
    @DisplayName("지출결의번호로 전송 목록을 조회할 수 있다")
    void getTransfersByPaymentRequestNo() throws Exception {
        // given: 동일한 지출결의번호를 가진 전송들이 주어졌을 때
        String paymentRequestNo = "PAY-2024-001";
        FtpTransfer transfer1 = FtpTransfer.create(paymentRequestNo, "path1", "file1.txt", "content1".getBytes());
        FtpTransfer transfer2 = FtpTransfer.create(paymentRequestNo, "path2", "file2.txt", "content2".getBytes());
        when(transferService.getTransfersByPaymentRequestNo(paymentRequestNo))
            .thenReturn(Arrays.asList(transfer1, transfer2));

        // when: 지출결의번호로 전송 목록을 요청하면
        // then: 전송 목록 페이지가 반환된다
        mockMvc.perform(get("/ftp/transfer/payment/{paymentRequestNo}", paymentRequestNo))
            .andExpect(status().isOk())
            .andExpect(view().name("ftp/transfer-list"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.model().attributeExists("transfers"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.model().attribute("paymentRequestNo", paymentRequestNo));
        
        log.info("지출결의번호로 전송 목록 조회 성공: paymentRequestNo={}", paymentRequestNo);
    }

    @Test
    @DisplayName("FTP 연결 테스트를 수행할 수 있다")
    void testConnection() throws Exception {
        // given: FTP 서버가 정상적으로 연결 가능할 때
        when(troubleshootingService.testConnection()).thenReturn(true);

        // when: 연결 테스트를 요청하면
        // then: 연결 성공 결과가 반환된다
        mockMvc.perform(get("/ftp/test-connection"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.connected").value(true))
            .andExpect(jsonPath("$.message").value("FTP 연결 성공"));
        
        log.info("FTP 연결 테스트 성공");
    }

    @Test
    @DisplayName("전송 통계를 조회할 수 있다")
    void getStatistics() throws Exception {
        // given: 통계 정보가 주어졌을 때
        var statistics = new FtpTroubleshootingService.FtpTransferStatistics(10, 8, 2, 0, 0, 0);
        when(troubleshootingService.getStatistics()).thenReturn(statistics);

        // when: 통계를 요청하면
        // then: 통계 정보가 반환된다
        mockMvc.perform(get("/ftp/statistics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(10))
            .andExpect(jsonPath("$.success").value(8))
            .andExpect(jsonPath("$.failed").value(2));
        
        log.info("전송 통계 조회 성공: total={}, success={}, failed={}", 
            statistics.getTotal(), statistics.getSuccess(), statistics.getFailed());
    }
}
