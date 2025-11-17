package com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class TossPaymentCancelResponse {
    @JsonProperty("mId")
    private String mId;

    @JsonProperty("lastTransactionKey")
    private String lastTransactionKey;

    @JsonProperty("paymentKey")
    private String paymentKey;

    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("orderName")
    private String orderName;

    @JsonProperty("taxExemptionAmount")
    private BigDecimal taxExemptionAmount;

    @JsonProperty("status")
    private String status;

    @JsonProperty("requestedAt")
    private String requestedAt;

    @JsonProperty("approvedAt")
    private String approvedAt;

    @JsonProperty("useEscrow")
    private Boolean useEscrow;

    @JsonProperty("cultureExpense")
    private Boolean cultureExpense;

    @JsonProperty("card")
    private CardInfo card;

    @JsonProperty("virtualAccount")
    private Object virtualAccount;

    @JsonProperty("transfer")
    private Object transfer;

    @JsonProperty("mobilePhone")
    private Object mobilePhone;

    @JsonProperty("giftCertificate")
    private Object giftCertificate;

    @JsonProperty("cashReceipt")
    private Object cashReceipt;

    @JsonProperty("cashReceipts")
    private List<CashReceiptInfo> cashReceipts;

    @JsonProperty("discount")
    private Object discount;

    @JsonProperty("cancels")
    private List<CancelInfo> cancels;

    @JsonProperty("secret")
    private String secret;

    @JsonProperty("type")
    private String type;

    @JsonProperty("easyPay")
    private EasyPayInfo easyPay;

    @JsonProperty("country")
    private String country;

    @JsonProperty("failure")
    private Object failure;

    @JsonProperty("isPartialCancelable")
    private Boolean isPartialCancelable;

    @JsonProperty("receipt")
    private ReceiptInfo receipt;

    @JsonProperty("checkout")
    private CheckoutInfo checkout;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("totalAmount")
    private BigDecimal totalAmount;

    @JsonProperty("balanceAmount")
    private BigDecimal balanceAmount;

    @JsonProperty("suppliedAmount")
    private BigDecimal suppliedAmount;

    @JsonProperty("vat")
    private BigDecimal vat;

    @JsonProperty("taxFreeAmount")
    private BigDecimal taxFreeAmount;

    @JsonProperty("method")
    private String method;

    @JsonProperty("version")
    private String version;

    @JsonProperty("metadata")
    private Object metadata;

    @Getter
    @Setter
    public static class CardInfo {
        @JsonProperty("issuerCode")
        private String issuerCode;

        @JsonProperty("acquirerCode")
        private String acquirerCode;

        @JsonProperty("number")
        private String number;

        @JsonProperty("installmentPlanMonths")
        private Integer installmentPlanMonths;

        @JsonProperty("isInterestFree")
        private Boolean isInterestFree;

        @JsonProperty("interestPayer")
        private String interestPayer;

        @JsonProperty("approveNo")
        private String approveNo;

        @JsonProperty("useCardPoint")
        private Boolean useCardPoint;

        @JsonProperty("cardType")
        private String cardType;

        @JsonProperty("ownerType")
        private String ownerType;

        @JsonProperty("acquireStatus")
        private String acquireStatus;

        @JsonProperty("amount")
        private BigDecimal amount;
    }

    @Getter
    @Setter
    public static class CashReceiptInfo {
        @JsonProperty("receiptKey")
        private String receiptKey;

        @JsonProperty("orderId")
        private String orderId;

        @JsonProperty("orderName")
        private String orderName;

        @JsonProperty("type")
        private String type;

        @JsonProperty("issueNumber")
        private String issueNumber;

        @JsonProperty("receiptUrl")
        private String receiptUrl;

        @JsonProperty("businessNumber")
        private String businessNumber;

        @JsonProperty("transactionType")
        private String transactionType;

        @JsonProperty("amount")
        private BigDecimal amount;

        @JsonProperty("taxFreeAmount")
        private BigDecimal taxFreeAmount;

        @JsonProperty("issueStatus")
        private String issueStatus;

        @JsonProperty("failure")
        private Object failure;

        @JsonProperty("requestedAt")
        private String requestedAt;

        @JsonProperty("customerIdentityNumber")
        private String customerIdentityNumber;
    }

    @Getter
    @Setter
    public static class CancelInfo {
        @JsonProperty("transactionKey")
        private String transactionKey;

        @JsonProperty("cancelReason")
        private String cancelReason;

        @JsonProperty("taxExemptionAmount")
        private BigDecimal taxExemptionAmount;

        @JsonProperty("canceledAt")
        private String canceledAt;

        @JsonProperty("transferDiscountAmount")
        private BigDecimal transferDiscountAmount;

        @JsonProperty("easyPayDiscountAmount")
        private BigDecimal easyPayDiscountAmount;

        @JsonProperty("receiptKey")
        private String receiptKey;

        @JsonProperty("cancelAmount")
        private BigDecimal cancelAmount;

        @JsonProperty("taxFreeAmount")
        private BigDecimal taxFreeAmount;

        @JsonProperty("refundableAmount")
        private BigDecimal refundableAmount;

        @JsonProperty("cancelStatus")
        private String cancelStatus;

        @JsonProperty("cancelRequestId")
        private String cancelRequestId;
    }

    @Getter
    @Setter
    public static class EasyPayInfo {
        @JsonProperty("provider")
        private String provider;

        @JsonProperty("amount")
        private BigDecimal amount;

        @JsonProperty("discountAmount")
        private BigDecimal discountAmount;
    }

    @Getter
    @Setter
    public static class ReceiptInfo {
        @JsonProperty("url")
        private String url;
    }

    @Getter
    @Setter
    public static class CheckoutInfo {
        @JsonProperty("url")
        private String url;
    }
}

