package com.sleekydz86.ftpserver.presentation.ftp;

import com.sleekydz86.ftpserver.application.ftp.FtpTransferCommand;
import com.sleekydz86.ftpserver.application.ftp.FtpTransferService;
import com.sleekydz86.ftpserver.application.ftp.FtpTroubleshootingService;
import com.sleekydz86.ftpserver.domain.ftp.FtpTransfer;
import com.sleekydz86.ftpserver.domain.ftp.FtpTransferId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/ftp")
@RequiredArgsConstructor
public class FtpTransferController {
    
    private final FtpTransferService transferService;
    private final FtpTroubleshootingService troubleshootingService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("command", new FtpTransferCommand());
        model.addAttribute("statistics", troubleshootingService.getStatistics());
        return "ftp/index";
    }

    @PostMapping("/transfer")
    public String requestTransfer(@ModelAttribute FtpTransferCommand command, Model model) {
        try {
            FtpTransferId transferId = transferService.requestTransfer(
                command.getPaymentRequestNo(),
                command.getRemotePath(),
                command.getFileName(),
                command.getFileContent()
            );
            model.addAttribute("transferId", transferId.getValue());
            model.addAttribute("message", "FTP 전송 요청이 생성되었습니다. 비동기로 처리됩니다.");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("command", new FtpTransferCommand());
        model.addAttribute("statistics", troubleshootingService.getStatistics());
        return "ftp/index";
    }

    @GetMapping("/transfer/{transferId}")
    public String getTransfer(@PathVariable String transferId, Model model) {
        try {
            FtpTransferId id = FtpTransferId.of(transferId);
            transferService.getTransfer(id).ifPresentOrElse(
                transfer -> {
                    model.addAttribute("transfer", transfer);
                    model.addAttribute("transferId", transferId);
                },
                () -> model.addAttribute("error", "전송을 찾을 수 없습니다.")
            );
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "ftp/transfer-detail";
    }

    @GetMapping("/transfer/payment/{paymentRequestNo}")
    public String getTransfersByPaymentRequestNo(@PathVariable String paymentRequestNo, Model model) {
        List<FtpTransfer> transfers = transferService.getTransfersByPaymentRequestNo(paymentRequestNo);
        model.addAttribute("transfers", transfers);
        model.addAttribute("paymentRequestNo", paymentRequestNo);
        return "ftp/transfer-list";
    }

    @PostMapping("/transfer/{transferId}/retry")
    public String retryTransfer(@PathVariable String transferId, Model model) {
        try {
            FtpTransferId id = FtpTransferId.of(transferId);
            troubleshootingService.retryTransfer(id);
            model.addAttribute("message", "FTP 전송 재시도가 시작되었습니다.");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/ftp/transfer/" + transferId;
    }

    @GetMapping("/test-connection")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testConnection() {
        boolean result = troubleshootingService.testConnection();
        Map<String, Object> response = new HashMap<>();
        response.put("connected", result);
        response.put("message", result ? "FTP 연결 성공" : "FTP 연결 실패");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    @ResponseBody
    public ResponseEntity<FtpTroubleshootingService.FtpTransferStatistics> getStatistics() {
        return ResponseEntity.ok(troubleshootingService.getStatistics());
    }
}
