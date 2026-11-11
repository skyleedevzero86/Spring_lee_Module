package com.sleekydz86.storagemigration.audit.application.port.in;

import com.sleekydz86.storagemigration.global.web.Actor;

public interface RecordAuditUseCase {

    void record(Actor actor, String action, String targetType, String targetId, String detail);
}
