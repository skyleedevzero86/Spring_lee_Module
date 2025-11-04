package com.sleekydz86.ocrstudy1.doamin.model;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdVerification {
    private Boolean isIdCard;
    private String documentType;
    private ExtractedInfo extractedInfo;
    private Double verificationConfidence;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtractedInfo {
        private String name;
        private String idNumber;
        private String dateOfBirth;
        private String address;
        private String expiryDate;
        private String issuingAuthority;
    }
}

