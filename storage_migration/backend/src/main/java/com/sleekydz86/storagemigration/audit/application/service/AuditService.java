package com.sleekydz86.storagemigration.audit.application.service;

import com.sleekydz86.storagemigration.audit.application.port.in.AuditLogQueryUseCase;
import com.sleekydz86.storagemigration.audit.application.port.in.RecordAuditUseCase;
import com.sleekydz86.storagemigration.audit.application.port.out.AuditLogRepository;
import com.sleekydz86.storagemigration.audit.domain.AuditLog;
import com.sleekydz86.storagemigration.audit.domain.AuditSearchCondition;
import com.sleekydz86.storagemigration.global.web.Actor;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditService implements RecordAuditUseCase, AuditLogQueryUseCase {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public void record(Actor actor, String action, String targetType, String targetId, String detail) {
        log.info("작업 로그 작업={} 대상={}:{} 작업자={} IP={} 내용={}", action, targetType, targetId,
                actor.worker(), actor.ip(), detail);
        if (!repository.available()) {
            return;
        }
        try {
            repository.record(actor, action, targetType, targetId, detail);
        } catch (RuntimeException exception) {
            log.warn("작업 로그를 DB에 남기지 못했습니다: {} ({})", action, exception.getMessage());
        }
    }

    @Override
    public List<AuditLog> search(AuditSearchCondition condition) {
        return repository.search(condition);
    }
}
