package com.sleekydz86.toaspayment.presentation;

import com.sleekydz86.toaspayment.application.dto.OrderResponse;
import com.sleekydz86.toaspayment.application.dto.PurchaseConfirmRequest;
import com.sleekydz86.toaspayment.application.dto.PurchaseInitRequest;
import com.sleekydz86.toaspayment.application.dto.PurchaseInitResponse;
import com.sleekydz86.toaspayment.application.dto.RefundRequest;
import com.sleekydz86.toaspayment.application.usecase.GetUserOrdersUseCase;
import com.sleekydz86.toaspayment.application.usecase.ConfirmPurchaseUseCase;
import com.sleekydz86.toaspayment.application.usecase.GenerateReceiptUseCase;
import com.sleekydz86.toaspayment.application.usecase.InitPurchaseUseCase;
import com.sleekydz86.toaspayment.application.usecase.RefundOrderUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "결제", description = "결제 관련 API")
@RestController
@RequestMapping("/api/v1/purchase")
@RequiredArgsConstructor
public class PaymentController {
    private final InitPurchaseUseCase initPurchaseUseCase;
    private final ConfirmPurchaseUseCase confirmPurchaseUseCase;
    private final RefundOrderUseCase refundOrderUseCase;
    private final GenerateReceiptUseCase generateReceiptUseCase;
    private final GetUserOrdersUseCase getUserOrdersUseCase;

    @Operation(summary = "결제 초기화", description = "결제를 시작하기 전 주문을 초기화합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/init")
    public ResponseEntity<PurchaseInitResponse> initPurchase(@Valid @RequestBody PurchaseInitRequest request) {
        PurchaseInitResponse response = initPurchaseUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "결제 승인", description = "결제를 승인합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmPurchase(@Valid @RequestBody PurchaseConfirmRequest request) {
        confirmPurchaseUseCase.execute(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "환불", description = "결제를 환불합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/refund")
    public ResponseEntity<Void> refundOrder(@Valid @RequestBody RefundRequest request) {
        refundOrderUseCase.execute(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "영수증 다운로드", description = "결제 영수증을 PDF로 다운로드합니다. (결제일로부터 14일 이내)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/receipt/{orderId}")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable String orderId) {
        byte[] pdfBytes = generateReceiptUseCase.execute(orderId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "영수증_" + orderId + ".pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @Operation(summary = "사용자 결제 기록 조회", description = "사용자가 자신의 결제 기록을 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getUserOrders() {
        List<OrderResponse> orders = getUserOrdersUseCase.execute();
        return ResponseEntity.ok(orders);
    }
}

