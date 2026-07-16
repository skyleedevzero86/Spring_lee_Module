package com.sleekydz86.catalogflow.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.batch")
public class BatchProperties {

	private int chunkSize = 100;
	private int csvSkipLimit = 100;
	private int csvRetryLimit = 3;
	private int tempImageRetentionDays = 7;
	private int cacheWarmupLimit = 50;
	private String reportMailTo = "ops@catalogflow.local";
	private boolean schedulerEnabled = true;
	private String emailProvider = "fake";
	private final Cron cron = new Cron();

	public int getChunkSize() {
		return chunkSize;
	}

	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}

	public int getCsvSkipLimit() {
		return csvSkipLimit;
	}

	public void setCsvSkipLimit(int csvSkipLimit) {
		this.csvSkipLimit = csvSkipLimit;
	}

	public int getCsvRetryLimit() {
		return csvRetryLimit;
	}

	public void setCsvRetryLimit(int csvRetryLimit) {
		this.csvRetryLimit = csvRetryLimit;
	}

	public int getTempImageRetentionDays() {
		return tempImageRetentionDays;
	}

	public void setTempImageRetentionDays(int tempImageRetentionDays) {
		this.tempImageRetentionDays = tempImageRetentionDays;
	}

	public int getCacheWarmupLimit() {
		return cacheWarmupLimit;
	}

	public void setCacheWarmupLimit(int cacheWarmupLimit) {
		this.cacheWarmupLimit = cacheWarmupLimit;
	}

	public String getReportMailTo() {
		return reportMailTo;
	}

	public void setReportMailTo(String reportMailTo) {
		this.reportMailTo = reportMailTo;
	}

	public boolean isSchedulerEnabled() {
		return schedulerEnabled;
	}

	public void setSchedulerEnabled(boolean schedulerEnabled) {
		this.schedulerEnabled = schedulerEnabled;
	}

	public String getEmailProvider() {
		return emailProvider;
	}

	public void setEmailProvider(String emailProvider) {
		this.emailProvider = emailProvider;
	}

	public Cron getCron() {
		return cron;
	}

	public static class Cron {

		private String csvImport = "-";
		private String readModelRebuild = "0 30 2 * * *";
		private String cacheWarmup = "0 0 3 * * *";
		private String aiRetry = "0 15 * * * *";
		private String tempImageCleanup = "0 0 4 * * *";
		private String dailyReport = "0 0 8 * * *";

		public String getCsvImport() {
			return csvImport;
		}

		public void setCsvImport(String csvImport) {
			this.csvImport = csvImport;
		}

		public String getReadModelRebuild() {
			return readModelRebuild;
		}

		public void setReadModelRebuild(String readModelRebuild) {
			this.readModelRebuild = readModelRebuild;
		}

		public String getCacheWarmup() {
			return cacheWarmup;
		}

		public void setCacheWarmup(String cacheWarmup) {
			this.cacheWarmup = cacheWarmup;
		}

		public String getAiRetry() {
			return aiRetry;
		}

		public void setAiRetry(String aiRetry) {
			this.aiRetry = aiRetry;
		}

		public String getTempImageCleanup() {
			return tempImageCleanup;
		}

		public void setTempImageCleanup(String tempImageCleanup) {
			this.tempImageCleanup = tempImageCleanup;
		}

		public String getDailyReport() {
			return dailyReport;
		}

		public void setDailyReport(String dailyReport) {
			this.dailyReport = dailyReport;
		}
	}
}
