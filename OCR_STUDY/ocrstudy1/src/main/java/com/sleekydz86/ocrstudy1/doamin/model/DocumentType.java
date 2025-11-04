package com.sleekydz86.ocrstudy1.doamin.model;

public enum DocumentType {
    RECEIPT("영수증"),
    ID_CARD("신분증"),
    DRIVER_LICENSE("운전면허증"),
    PASSPORT("여권"),
    INVOICE("세금계산서"),
    CONTRACT("계약서"),
    CERTIFICATE("증명서"),
    ETC("기타");

    private final String description;

    DocumentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

