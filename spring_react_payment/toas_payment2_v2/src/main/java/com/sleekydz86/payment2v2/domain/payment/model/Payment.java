package com.sleekydz86.payment2v2.domain.payment.model;

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
    @Index(name = "idx_pay_token", columnList = "payToken")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String orderNo;

    @Column(length = 30)
    private String payToken;

    @Column(nullable = false, length = 255)
    private String productDesc;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal amountTaxFree;

    private BigDecimal amountTaxable;

    private BigDecimal amountVat;

    private BigDecimal amountServiceFee;

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

    @Column(length = 10)
    private Integer discountedAmount;

    @Column(length = 10)
    private Integer paidAmount;

    @Column(length = 20)
    private String paidTs;

    @Column(length = 36)
    private String transactionId;

    @Column(length = 2)
    private Integer cardCompanyCode;

    @Column(length = 8)
    private String cardAuthorizationNo;

    @Column(length = 8)
    private String spreadOut;

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
    public Payment(String orderNo, String payToken, String productDesc, BigDecimal amount,
                   BigDecimal amountTaxFree, BigDecimal amountTaxable, BigDecimal amountVat,
                   BigDecimal amountServiceFee, BigDecimal disposableCupDeposit,
                   String retUrl, String retCancelUrl, String retAppScheme,
                   String resultCallback, String callbackVersion, LocalDateTime expiredTime,
                   String checkoutPage) {
        this.orderNo = orderNo;
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
        this.checkoutPage = checkoutPage;
        this.payToken = payToken;
    }

    public void completePayment(String payMethod, Integer discountedAmount, Integer paidAmount,
                                String paidTs, String transactionId) {
        this.status = PaymentStatus.COMPLETED;
        this.payMethod = payMethod;
        this.discountedAmount = discountedAmount;
        this.paidAmount = paidAmount;
        this.paidTs = paidTs;
        this.transactionId = transactionId;
    }

    public void updateCardInfo(Integer cardCompanyCode, String cardAuthorizationNo,
                               String spreadOut, Boolean noInterest, String cardMethodType,
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
                              Integer discountedAmount, Integer paidAmount, String transactionId,
                              String cashReceiptMgtKey) {
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
        this.spreadOut = spreadOut != null ? String.valueOf(spreadOut) : null;
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

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredTime);
    }
}
