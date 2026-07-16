package com.sleekydz86.catalogflow.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

	private String provider = "stub";
	private boolean fallbackEnabled = false;
	private String modelName = "stub-enrichment-v1";
	private String promptVersion = "stub-prompt-v1";
	private final Ollama ollama = new Ollama();
	private final Gemini gemini = new Gemini();

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

	public Ollama getOllama() {
		return ollama;
	}

	public Gemini getGemini() {
		return gemini;
	}

	public static class Ollama {

		private String baseUrl = "http://localhost:11434";
		private String model = "llama3.2";
		private double temperature = 0.2d;

		public String getBaseUrl() {
			return baseUrl;
		}

		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}

		public String getModel() {
			return model;
		}

		public void setModel(String model) {
			this.model = model;
		}

		public double getTemperature() {
			return temperature;
		}

		public void setTemperature(double temperature) {
			this.temperature = temperature;
		}
	}

	public static class Gemini {

		private String apiKey = "";
		private String model = "gemini-2.0-flash";
		private double temperature = 0.2d;

		public String getApiKey() {
			return apiKey;
		}

		public void setApiKey(String apiKey) {
			this.apiKey = apiKey;
		}

		public String getModel() {
			return model;
		}

		public void setModel(String model) {
			this.model = model;
		}

		public double getTemperature() {
			return temperature;
		}

		public void setTemperature(double temperature) {
			this.temperature = temperature;
		}
	}
}
