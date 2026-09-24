package com.sleekydz86.opspilot.incident.application.port.in;

import com.sleekydz86.opspilot.incident.domain.IncidentId;
import com.sleekydz86.opspilot.incident.domain.IncidentReport;

import java.util.List;
import java.util.Optional;

public interface QueryIncidentUseCase {

	Optional<IncidentReport> findById(IncidentId id);

	List<IncidentReport> findRecent(int limit);
}
