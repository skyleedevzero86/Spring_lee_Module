package com.sleekydz86.catalogflow.application.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.sleekydz86.catalogflow.adapter.in.batch.config.CatalogMaintenanceJobConfig;
import com.sleekydz86.catalogflow.adapter.in.batch.config.ProductCsvImportJobConfig;
import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.stereotype.Service;

@Service
public class BatchJobLaunchService {

	private final JobOperator jobOperator;
	private final JobRepository jobRepository;
	private final Map<String, Job> jobs;

	public BatchJobLaunchService(JobOperator jobOperator, JobRepository jobRepository, Map<String, Job> jobs) {
		this.jobOperator = jobOperator;
		this.jobRepository = jobRepository;
		this.jobs = jobs;
	}

	public JobExecution launchCsvImport(String filePath) {
		if (filePath == null || filePath.isBlank()) {
			throw new ApplicationException("CSV 파일 경로는 필수입니다");
		}
		if (!Files.exists(Path.of(filePath))) {
			throw new ApplicationException("CSV 파일을 찾을 수 없습니다: " + filePath);
		}
		JobParameters parameters = new JobParametersBuilder()
				.addString(ProductCsvImportJobConfig.FILE_PATH_PARAM, filePath)
				.addString("runId", UUID.randomUUID().toString())
				.toJobParameters();
		return launch(ProductCsvImportJobConfig.JOB_NAME, parameters);
	}

	public JobExecution launch(String jobName) {
		JobParameters parameters = new JobParametersBuilder()
				.addString("runId", UUID.randomUUID().toString())
				.addString("requestedAt", Instant.now().toString())
				.toJobParameters();
		return launch(jobName, parameters);
	}

	public JobExecution getExecution(long executionId) {
		JobExecution execution = jobRepository.getJobExecution(executionId);
		if (execution == null) {
			throw new ApplicationException("배치 실행 이력을 찾을 수 없습니다: " + executionId);
		}
		return execution;
	}

	private JobExecution launch(String jobName, JobParameters parameters) {
		Job job = jobs.get(jobName);
		if (job == null) {
			throw new ApplicationException("지원하지 않는 배치 작업입니다: " + jobName);
		}
		try {
			return jobOperator.start(job, parameters);
		}
		catch (Exception exception) {
			throw new ApplicationException("배치 작업 실행에 실패했습니다: " + jobName, exception);
		}
	}

	public static boolean isKnownJob(String jobName) {
		return ProductCsvImportJobConfig.JOB_NAME.equals(jobName)
				|| CatalogMaintenanceJobConfig.READ_MODEL_REBUILD_JOB.equals(jobName)
				|| CatalogMaintenanceJobConfig.CACHE_WARMUP_JOB.equals(jobName)
				|| CatalogMaintenanceJobConfig.AI_RETRY_JOB.equals(jobName)
				|| CatalogMaintenanceJobConfig.TEMP_IMAGE_CLEANUP_JOB.equals(jobName)
				|| CatalogMaintenanceJobConfig.DAILY_REPORT_JOB.equals(jobName);
	}
}
