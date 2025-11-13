package com.sleekydz86.payment2v2.domain.payment.model;

import com.sleekydz86.payment2v2.domain.payment.model.valueobject.OrderNo;
import com.sleekydz86.payment2v2.global.constants.PaymentConstants;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_order_no", columnList = "orderNo", unique = true),
    @Index(name = "idx_pay_token", columnList = "payToken"),
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_user_id_created_at", columnList = "userId,createdAt")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Embedded
    private OrderNo orderNo;

    @Column(length = 30)
    private String payToken;

    @Column(nullable = false, length = 255)
    private String productDesc;

    @Column(nullable = false, precision = PaymentConstants.AMOUNT_PRECISION, scale = PaymentConstants.AMOUNT_SCALE)
    private BigDecimal amount;

    @Column(nullable = false, precision = PaymentConstants.AMOUNT_PRECISION, scale = PaymentConstants.AMOUNT_SCALE)
    private BigDecimal amountTaxFree;

    @Column(precision = PaymentConstants.AMOUNT_PRECISION, scale = PaymentConstants.AMOUNT_SCALE)
    private BigDecimal amountTaxable;

    @Column(precision = PaymentConstants.AMOUNT_PRECISION, scale = PaymentConstants.AMOUNT_SCALE)
    private BigDecimal amountVat;

    @Column(precision = PaymentConstants.AMOUNT_PRECISION, scale = PaymentConstants.AMOUNT_SCALE)
    private BigDecimal amountServiceFee;

    @Column(precision = PaymentConstants.AMOUNT_PRECISION, scale = PaymentConstants.AMOUNT_SCALE)
    private BigDecimal disposableCupDeposit;

    @Column(nullable = false, length = 255)
    private String retUrl;

    @Column(nullable = false, length = 255)
    private String retCancelUrl;

    @Column(length = 255)
    private String retAppScheme;

    @Column(length = 500)
    private String resultCallback;

    @Column(length = 2)
    private String callbackVersion;

    @Column(nullable = false)
    private LocalDateTime expiredTime;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 255)
    private String checkoutPage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(length = 10)
    private String payMethod;

    @Column(precision = PaymentConstants.AMOUNT_PRECISION, scale = PaymentConstants.AMOUNT_SCALE)
    private BigDecimal discountedAmount;

    @Column(precision = PaymentConstants.AMOUNT_PRECISION, scale = PaymentConstants.AMOUNT_SCALE)
    private BigDecimal paidAmount;

    @Column(length = 20)
    private String paidTs;

    @Column(length = 36)
    private String transactionId;

    @Column(length = 2)
    private Integer cardCompanyCode;

    @Column(length = 8)
    private String cardAuthorizationNo;

    @Column
    private Integer spreadOut;

    private Boolean noInterest;

    @Column(length = 10)
    private String cardMethodType;

    @Column(length = 20)
    private String cardUserType;

    @Column(length = 8)
    private String cardBinNumber;

    @Column(length = 4)
    private String cardNum4Print;

    @Column(length = 255)
    private String salesCheckLinkUrl;

    @Column(length = 3)
    private String accountBankCode;

    @Column(length = 20)
    private String accountBankName;

    @Column(length = 30)
    private String accountNumber;

    @Column(length = 4)
    private String mode;

    @Column(length = 20)
    private String approvalTime;

    @Column(length = 120)
    private String stateMsg;

    @Column(length = 36)
    private String cashReceiptMgtKey;

    @Column(length = 20)
    private String cardCompanyName;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = PaymentStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Builder
    public Payment(Long userId, String orderNo, String payToken, String productDesc, BigDecimal amount,
                   BigDecimal amountTaxFree, BigDecimal amountTaxable, BigDecimal amountVat,
                   BigDecimal amountServiceFee, BigDecimal disposableCupDeposit,
                   String retUrl, String retCancelUrl, String retAppScheme,
                   String resultCallback, String callbackVersion, LocalDateTime expiredTime,
                   String checkoutPage) {
        this.userId = userId;
        this.orderNo = OrderNo.of(orderNo);
        this.payToken = payToken;
        this.productDesc = productDesc;
        this.amount = amount;
        this.amountTaxFree = amountTaxFree;
        this.amountTaxable = amountTaxable;
        this.amountVat = amountVat;
        this.amountServiceFee = amountServiceFee;
        this.disposableCupDeposit = disposableCupDeposit;
        this.retUrl = retUrl;
        this.retCancelUrl = retCancelUrl;
        this.retAppScheme = retAppScheme;
        this.resultCallback = resultCallback;
        this.callbackVersion = callbackVersion;
        this.expiredTime = expiredTime;
        this.checkoutPage = checkoutPage;
        this.status = PaymentStatus.PENDING;
    }

    public void updateCheckoutInfo(String checkoutPage, String payToken) {
        if (checkoutPage == null || payToken == null) {
            throw new IllegalArgumentException("checkoutPage와 payToken은 필수입니다.");
        }
        this.checkoutPage = checkoutPage;
        this.payToken = payToken;
    }

    public void completePayment(String payMethod, BigDecimal discountedAmount, BigDecimal paidAmount,
                                String paidTs, String transactionId) {
        if (payMethod == null || paidTs == null || transactionId == null) {
            throw new IllegalArgumentException("payMethod, paidTs, transactionId는 필수입니다.");
        }
        this.status = PaymentStatus.COMPLETED;
        this.payMethod = payMethod;
        this.discountedAmount = discountedAmount;
        this.paidAmount = paidAmount;
        this.paidTs = paidTs;
        this.transactionId = transactionId;
    }

    public void updateCardInfo(Integer cardCompanyCode, String cardAuthorizationNo,
                               Integer spreadOut, Boolean noInterest, String cardMethodType,
                               String cardUserType, String cardBinNumber, String cardNum4Print,
                               String salesCheckLinkUrl) {
        this.cardCompanyCode = cardCompanyCode;
        this.cardAuthorizationNo = cardAuthorizationNo;
        this.spreadOut = spreadOut;
        this.noInterest = noInterest;
        this.cardMethodType = cardMethodType;
        this.cardUserType = cardUserType;
        this.cardBinNumber = cardBinNumber;
        this.cardNum4Print = cardNum4Print;
        this.salesCheckLinkUrl = salesCheckLinkUrl;
    }

    public void updateAccountInfo(String accountBankCode, String accountBankName, String accountNumber) {
        this.accountBankCode = accountBankCode;
        this.accountBankName = accountBankName;
        this.accountNumber = accountNumber;
    }

    public void approve() {
        this.status = PaymentStatus.APPROVED;
    }

    public void approvePayment(String mode, String approvalTime, String stateMsg, String payMethod,
                              BigDecimal discountedAmount, BigDecimal paidAmount, String transactionId,
                              String cashReceiptMgtKey) {
        if (mode == null || approvalTime == null || stateMsg == null || payMethod == null || transactionId == null) {
            throw new IllegalArgumentException("mode, approvalTime, stateMsg, payMethod, transactionId는 필수입니다.");
        }
        this.status = PaymentStatus.COMPLETED;
        this.mode = mode;
        this.approvalTime = approvalTime;
        this.stateMsg = stateMsg;
        this.payMethod = payMethod;
        this.discountedAmount = discountedAmount;
        this.paidAmount = paidAmount;
        this.transactionId = transactionId;
        this.cashReceiptMgtKey = cashReceiptMgtKey;
    }

    public void updateApprovalCardInfo(String cardCompanyName, Integer cardCompanyCode,
                                      String cardAuthorizationNo, Integer spreadOut,
                                      Boolean noInterest, String salesCheckLinkUrl,
                                      String cardMethodType, String cardNumber,
                                      String cardUserType, String cardBinNumber,
                                      String cardNum4Print) {
        this.cardCompanyName = cardCompanyName;
        this.cardCompanyCode = cardCompanyCode;
        this.cardAuthorizationNo = cardAuthorizationNo;
        this.spreadOut = spreadOut;
        this.noInterest = noInterest;
        this.salesCheckLinkUrl = salesCheckLinkUrl;
        this.cardMethodType = cardMethodType;
        this.cardUserType = cardUserType;
        this.cardBinNumber = cardBinNumber;
        this.cardNum4Print = cardNum4Print;
    }

    public void updateApprovalAccountInfo(String accountBankCode, String accountBankName, String accountNumber) {
        this.accountBankCode = accountBankCode;
        this.accountBankName = accountBankName;
        this.accountNumber = accountNumber;
    }

    public void cancel() {
        this.status = PaymentStatus.CANCELLED;
    }

    public void refund(String refundNo, Integer refundableAmount, Integer refundedAmount,
                       String approvalTime, String transactionId) {
        if (refundNo == null || approvalTime == null) {
            throw new IllegalArgumentException("refundNo, approvalTime는 필수입니다.");
        }
        this.status = PaymentStatus.CANCELLED;
    }

    public boolean isExpired() {
        return expiredTime != null && LocalDateTime.now().isAfter(expiredTime);
    }

    public boolean canRefund() {
        return PaymentStatus.COMPLETED.equals(this.status);
    }

    public String getOrderNoValue() {
        return orderNo != null ? orderNo.getValue() : null;
    }

    public void validateForApproval() {
        if (PaymentStatus.COMPLETED.equals(this.status)) {
            throw new IllegalArgumentException(
                    String.format("이미 완료된 결제입니다. orderNo: %s", this.getOrderNoValue()));
        }

        if (!PaymentStatus.PENDING.equals(this.status) && !PaymentStatus.APPROVED.equals(this.status)) {
            throw new IllegalArgumentException(
                    String.format("결제 승인 대기 상태가 아닙니다. 현재 상태: %s, orderNo: %s",
                            this.status, this.getOrderNoValue()));
        }
    }

    public void validatePayToken(String payToken) {
        if (this.payToken != null && !this.payToken.equals(payToken)) {
            throw new IllegalArgumentException(
                    String.format("결제 토큰이 일치하지 않습니다. orderNo: %s", this.getOrderNoValue()));
        }
    }

    public void validateForRefund() {
        if (!canRefund()) {
            throw new IllegalArgumentException(
                    String.format("환불 가능한 상태가 아닙니다. 현재 상태: %s, orderNo: %s",
                            this.status, this.getOrderNoValue()));
        }

        if (this.payToken == null || this.payToken.isBlank()) {
            throw new IllegalArgumentException(
                    String.format("결제 토큰이 없습니다. orderNo: %s", this.getOrderNoValue()));
        }
    }

    public void validateRefundAmount(java.math.BigDecimal refundAmount) {
        if (refundAmount == null) {
            return;
        }

        if (this.amount == null) {
            throw new IllegalStateException(
                    String.format("결제 금액이 null입니다. paymentId: %d", this.id));
        }

        if (refundAmount.compareTo(this.amount) > 0) {
            throw new IllegalArgumentException(
                    String.format("환불 요청 금액이 결제 금액을 초과합니다. paymentId: %d, paymentAmount: %s, refundAmount: %s",
                            this.id, this.amount, refundAmount));
        }

        if (refundAmount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    String.format("환불 요청 금액은 0보다 커야 합니다. refundAmount: %s", refundAmount));
        }
    }
}
