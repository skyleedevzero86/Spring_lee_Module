package com.sleekydz86.catalogflow.adapter.in.web;

import com.sleekydz86.catalogflow.adapter.in.web.dto.BatchJobExecutionResponse;
import com.sleekydz86.catalogflow.adapter.in.web.dto.LaunchCsvImportRequest;
import com.sleekydz86.catalogflow.application.service.BatchJobLaunchService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/batches")
public class BatchJobController {

	private final BatchJobLaunchService batchJobLaunchService;

	public BatchJobController(BatchJobLaunchService batchJobLaunchService) {
		this.batchJobLaunchService = batchJobLaunchService;
	}

	@PostMapping("/csv-import")
	public ResponseEntity<BatchJobExecutionResponse> launchCsvImport(@Valid @RequestBody LaunchCsvImportRequest request) {
		var execution = batchJobLaunchService.launchCsvImport(request.filePath());
		return ResponseEntity.accepted().body(BatchJobExecutionResponse.from(execution));
	}

	@PostMapping("/{jobName}/run")
	public ResponseEntity<BatchJobExecutionResponse> launch(@PathVariable String jobName) {
		if (!BatchJobLaunchService.isKnownJob(jobName)) {
			return ResponseEntity.badRequest().build();
		}
		var execution = batchJobLaunchService.launch(jobName);
		return ResponseEntity.accepted().body(BatchJobExecutionResponse.from(execution));
	}

	@GetMapping("/executions/{executionId}")
	public BatchJobExecutionResponse getExecution(@PathVariable long executionId) {
		return BatchJobExecutionResponse.from(batchJobLaunchService.getExecution(executionId));
	}
}
