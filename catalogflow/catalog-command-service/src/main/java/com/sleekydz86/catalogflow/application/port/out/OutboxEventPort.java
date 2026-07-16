package com.sleekydz86.catalogflow.application.port.out;

import com.sleekydz86.catalogflow.domain.event.DomainEvent;

import java.util.List;

public interface OutboxEventPort {

	void saveAll(List<DomainEvent> events);
}
