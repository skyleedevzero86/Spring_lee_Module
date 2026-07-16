package com.sleekydz86.catalogflow.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.sleekydz86.catalogflow.adapter.in.batch.config.ProductCsvImportJobConfig;
import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;

class BatchJobLaunchServiceTest {

	@TempDir
	Path tempDir;

	@Test
	@DisplayName("CSV 파일이 없으면 배치 실행을 거부한다")
	void shouldRejectMissingCsvFile() {
		// given
		BatchJobLaunchService service = new BatchJobLaunchService(
				mock(JobOperator.class),
				mock(JobRepository.class),
				Map.of());

		// when / then
		ApplicationException exception = assertThrows(
				ApplicationException.class,
				() -> service.launchCsvImport(tempDir.resolve("missing.csv").toString()));
		assertEquals(true, exception.getMessage().contains("CSV 파일을 찾을 수 없습니다"));
	}

	@Test
	@DisplayName("CSV 파일이 있으면 Import Job을 실행한다")
	void shouldLaunchCsvImportJob() throws Exception {
		// given
		Path csv = tempDir.resolve("products.csv");
		Files.writeString(csv, "productCode,name\nP-1,테스트\n");
		JobOperator jobOperator = mock(JobOperator.class);
		Job job = mock(Job.class);
		JobExecution execution = mock(JobExecution.class);
		when(jobOperator.start(eq(job), any(JobParameters.class))).thenReturn(execution);
		BatchJobLaunchService service = new BatchJobLaunchService(
				jobOperator,
				mock(JobRepository.class),
				Map.of(ProductCsvImportJobConfig.JOB_NAME, job));

		// when
		JobExecution result = service.launchCsvImport(csv.toString());

		// then
		assertEquals(execution, result);
	}

	@Test
	@DisplayName("알 수 없는 Job 이름은 예외를 발생시킨다")
	void shouldRejectUnknownJobName() {
		// given
		BatchJobLaunchService service = new BatchJobLaunchService(
				mock(JobOperator.class),
				mock(JobRepository.class),
				Map.of());

		// when / then
		assertThrows(ApplicationException.class, () -> service.launch("unknownJob"));
	}
}
