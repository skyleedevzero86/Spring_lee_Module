package com.sleekydz86.catalogflow.adapter.in.batch.config;

import java.util.Set;

import com.sleekydz86.catalogflow.adapter.in.batch.csv.ProductCsvFieldSetMapper;
import com.sleekydz86.catalogflow.adapter.in.batch.csv.ProductCsvItemProcessor;
import com.sleekydz86.catalogflow.adapter.in.batch.csv.ProductCsvItemWriter;
import com.sleekydz86.catalogflow.application.batch.model.ProductCsvRow;
import com.sleekydz86.catalogflow.application.batch.model.ProductImportItem;
import com.sleekydz86.catalogflow.global.config.BatchProperties;
import com.sleekydz86.catalogflow.global.exception.ProductCsvValidationException;
import com.sleekydz86.catalogflow.global.exception.TransientBatchException;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.infrastructure.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ProductCsvImportJobConfig {

	public static final String JOB_NAME = "productCsvImportJob";
	public static final String STEP_NAME = "productCsvImportStep";
	public static final String FILE_PATH_PARAM = "filePath";

	@Bean
	@StepScope
	FlatFileItemReader<ProductCsvRow> productCsvItemReader(
			@Value("#{jobParameters['filePath']}") String filePath) {
		DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
		tokenizer.setNames(
				"productCode",
				"name",
				"description",
				"priceAmount",
				"priceCurrency",
				"categoryId",
				"supplierId");
		tokenizer.setStrict(false);

		DefaultLineMapper<ProductCsvRow> lineMapper = new DefaultLineMapper<>();
		lineMapper.setLineTokenizer(tokenizer);
		lineMapper.setFieldSetMapper(new ProductCsvFieldSetMapper());

		return new FlatFileItemReaderBuilder<ProductCsvRow>()
				.name("productCsvItemReader")
				.resource(new FileSystemResource(filePath))
				.linesToSkip(1)
				.lineMapper(lineMapper)
				.build();
	}

	@Bean
	Step productCsvImportStep(
			JobRepository jobRepository,
			PlatformTransactionManager transactionManager,
			FlatFileItemReader<ProductCsvRow> productCsvItemReader,
			ProductCsvItemProcessor productCsvItemProcessor,
			ProductCsvItemWriter productCsvItemWriter,
			BatchProperties batchProperties) {
		RetryPolicy retryPolicy = RetryPolicy.builder()
				.maxRetries(batchProperties.getCsvRetryLimit())
				.includes(Set.of(TransientBatchException.class))
				.build();
		return new ChunkOrientedStepBuilder<ProductCsvRow, ProductImportItem>(
				STEP_NAME,
				jobRepository,
				batchProperties.getChunkSize())
				.reader(productCsvItemReader)
				.processor(productCsvItemProcessor)
				.writer(productCsvItemWriter)
				.transactionManager(transactionManager)
				.faultTolerant()
				.retryPolicy(retryPolicy)
				.skipPolicy((throwable, skipCount) ->
						throwable instanceof ProductCsvValidationException
								&& skipCount < batchProperties.getCsvSkipLimit())
				.build();
	}

	@Bean
	Job productCsvImportJob(JobRepository jobRepository, Step productCsvImportStep) {
		return new JobBuilder(JOB_NAME, jobRepository)
				.start(productCsvImportStep)
				.build();
	}
}
