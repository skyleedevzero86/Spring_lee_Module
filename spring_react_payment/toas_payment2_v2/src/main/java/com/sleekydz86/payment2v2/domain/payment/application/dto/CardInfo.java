package com.sleekydz86.payment2v2.domain.payment.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardInfo {
    private Boolean noInterest;
    private Integer spreadOut;
    private String cardAuthorizationNo;
    private String cardMethodType;
    private String cardUserType;
    private String cardNumber;
    private String cardBinNumber;
    private String cardNum4Print;
    private String salesCheckLinkUrl;
    private String cardCompanyName;
    private Integer cardCompanyCode;
}

