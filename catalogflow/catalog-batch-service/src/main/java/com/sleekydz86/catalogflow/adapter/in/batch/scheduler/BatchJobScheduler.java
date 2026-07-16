package com.sleekydz86.catalogflow.adapter.in.batch.scheduler;

import com.sleekydz86.catalogflow.adapter.in.batch.config.CatalogMaintenanceJobConfig;
import com.sleekydz86.catalogflow.application.service.BatchJobLaunchService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.batch.scheduler-enabled", havingValue = "true", matchIfMissing = true)
public class BatchJobScheduler {

	private final BatchJobLaunchService batchJobLaunchService;

	public BatchJobScheduler(BatchJobLaunchService batchJobLaunchService) {
		this.batchJobLaunchService = batchJobLaunchService;
	}

	@Scheduled(cron = "${app.batch.cron.read-model-rebuild:0 30 2 * * *}")
	public void scheduleReadModelRebuild() {
		batchJobLaunchService.launch(CatalogMaintenanceJobConfig.READ_MODEL_REBUILD_JOB);
	}

	@Scheduled(cron = "${app.batch.cron.cache-warmup:0 0 3 * * *}")
	public void scheduleCacheWarmup() {
		batchJobLaunchService.launch(CatalogMaintenanceJobConfig.CACHE_WARMUP_JOB);
	}

	@Scheduled(cron = "${app.batch.cron.ai-retry:0 15 * * * *}")
	public void scheduleAiRetry() {
		batchJobLaunchService.launch(CatalogMaintenanceJobConfig.AI_RETRY_JOB);
	}

	@Scheduled(cron = "${app.batch.cron.temp-image-cleanup:0 0 4 * * *}")
	public void scheduleTempImageCleanup() {
		batchJobLaunchService.launch(CatalogMaintenanceJobConfig.TEMP_IMAGE_CLEANUP_JOB);
	}

	@Scheduled(cron = "${app.batch.cron.daily-report:0 0 8 * * *}")
	public void scheduleDailyReport() {
		batchJobLaunchService.launch(CatalogMaintenanceJobConfig.DAILY_REPORT_JOB);
	}
}
