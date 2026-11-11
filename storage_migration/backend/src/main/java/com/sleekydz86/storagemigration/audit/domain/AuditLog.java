package com.sleekydz86.storagemigration.audit.domain;

import java.time.Instant;

public record AuditLog(long id, String action, String targetType, String targetId, String detail, String worker,
                       String workIp, Instant workedAt) {
}
