package com.sleekydz86.toaspayment.presentation;

import com.sleekydz86.toaspayment.application.dto.PurchaseConfirmRequest;
import com.sleekydz86.toaspayment.application.dto.PurchaseInitRequest;
import com.sleekydz86.toaspayment.application.dto.PurchaseInitResponse;
import com.sleekydz86.toaspayment.application.dto.RefundRequest;
import com.sleekydz86.toaspayment.application.usecase.ConfirmPurchaseUseCase;
import com.sleekydz86.toaspayment.application.usecase.InitPurchaseUseCase;
import com.sleekydz86.toaspayment.application.usecase.RefundOrderUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
}

