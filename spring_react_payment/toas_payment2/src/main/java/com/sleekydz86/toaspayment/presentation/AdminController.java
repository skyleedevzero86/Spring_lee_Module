package com.sleekydz86.toaspayment.presentation;

import com.sleekydz86.toaspayment.application.dto.OrderResponse;
import com.sleekydz86.toaspayment.application.dto.PaymentLogResponse;
import com.sleekydz86.toaspayment.application.dto.SearchOrdersRequest;
import com.sleekydz86.toaspayment.application.usecase.GetAllOrdersUseCase;
import com.sleekydz86.toaspayment.application.usecase.GetOrderLogsUseCase;
import com.sleekydz86.toaspayment.application.usecase.SearchOrdersUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "관리자", description = "관리자 전용 API")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final GetAllOrdersUseCase getAllOrdersUseCase;
    private final SearchOrdersUseCase searchOrdersUseCase;
    private final GetOrderLogsUseCase getOrderLogsUseCase;

    @Operation(summary = "모든 결제 기록 조회", description = "관리자가 모든 사용자의 결제 기록을 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = getAllOrdersUseCase.execute();
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "결제 검색", description = "관리자가 결제 기록을 검색합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/orders/search")
    public ResponseEntity<List<OrderResponse>> searchOrders(
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        SearchOrdersRequest request = new SearchOrdersRequest(
                orderId,
                memberId,
                status,
                startDate != null ? java.time.LocalDateTime.parse(startDate) : null,
                endDate != null ? java.time.LocalDateTime.parse(endDate) : null
        );
        List<OrderResponse> orders = searchOrdersUseCase.execute(request);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "주문 로그 조회", description = "주문 ID로 해당 주문의 로그를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/orders/{orderId}/logs")
    public ResponseEntity<List<PaymentLogResponse>> getOrderLogs(@PathVariable String orderId) {
        List<PaymentLogResponse> logs = getOrderLogsUseCase.execute(orderId);
        return ResponseEntity.ok(logs);
    }
}
