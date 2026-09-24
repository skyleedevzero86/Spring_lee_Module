package com.sleekydz86.opspilot.incident.application.port.in;

import com.sleekydz86.opspilot.incident.domain.IncidentReport;

public interface TriageIncidentUseCase {

	IncidentReport triage(TriageIncidentCommand command);
}
