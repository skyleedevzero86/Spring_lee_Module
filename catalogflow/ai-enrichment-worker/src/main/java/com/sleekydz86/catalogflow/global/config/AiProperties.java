package com.sleekydz86.catalogflow.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

	private String provider = "stub";
	private boolean fallbackEnabled = false;
	private String modelName = "stub-enrichment-v1";
	private String promptVersion = "stub-prompt-v1";

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public boolean isFallbackEnabled() {
		return fallbackEnabled;
	}

	public void setFallbackEnabled(boolean fallbackEnabled) {
		this.fallbackEnabled = fallbackEnabled;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getPromptVersion() {
		return promptVersion;
	}

	public void setPromptVersion(String promptVersion) {
		this.promptVersion = promptVersion;
	}
}
