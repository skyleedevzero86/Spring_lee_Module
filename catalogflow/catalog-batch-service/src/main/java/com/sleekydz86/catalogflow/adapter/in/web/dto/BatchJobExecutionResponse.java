package com.sleekydz86.catalogflow.adapter.in.web.dto;

import java.time.LocalDateTime;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;

public record BatchJobExecutionResponse(
		long executionId,
		String jobName,
		String status,
		String startTime,
		String endTime,
		String exitCode,
		String exitDescription) {

	public static BatchJobExecutionResponse from(JobExecution execution) {
		BatchStatus status = execution.getStatus();
		return new BatchJobExecutionResponse(
				execution.getId(),
				execution.getJobInstance().getJobName(),
				status == null ? "" : status.name(),
				stringify(execution.getStartTime()),
				stringify(execution.getEndTime()),
				execution.getExitStatus() == null ? "" : execution.getExitStatus().getExitCode(),
				execution.getExitStatus() == null ? "" : execution.getExitStatus().getExitDescription());
	}

	private static String stringify(LocalDateTime value) {
		return value == null ? "" : value.toString();
	}
}
