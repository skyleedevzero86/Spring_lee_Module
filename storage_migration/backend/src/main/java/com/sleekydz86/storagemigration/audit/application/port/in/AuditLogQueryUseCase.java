package com.sleekydz86.storagemigration.audit.application.port.in;

import com.sleekydz86.storagemigration.audit.domain.AuditLog;
import com.sleekydz86.storagemigration.audit.domain.AuditSearchCondition;
import java.util.List;

public interface AuditLogQueryUseCase {

    List<AuditLog> search(AuditSearchCondition condition);
}
