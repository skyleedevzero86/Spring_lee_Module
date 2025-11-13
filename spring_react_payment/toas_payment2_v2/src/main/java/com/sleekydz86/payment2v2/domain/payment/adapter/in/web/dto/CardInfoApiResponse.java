package com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CardInfoApiResponse {
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

