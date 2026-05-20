package com.sleekydz86.monitoring.logstack_s3.infrastructure.persistence.model;

public enum FileOperation {

    C,
    U,
    D,
    S;

    public String code() {
        return name();
    }
}
