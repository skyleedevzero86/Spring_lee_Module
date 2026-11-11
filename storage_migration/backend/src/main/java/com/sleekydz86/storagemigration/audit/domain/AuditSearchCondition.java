package com.sleekydz86.storagemigration.audit.domain;

public record AuditSearchCondition(String targetType, String targetId, String worker, int limit) {

    public AuditSearchCondition {
        if (limit <= 0 || limit > 1000) {
            limit = 200;
        }
    }
}
