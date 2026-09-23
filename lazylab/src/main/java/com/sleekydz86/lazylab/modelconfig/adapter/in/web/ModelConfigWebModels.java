package com.sleekydz86.lazylab.modelconfig.adapter.in.web;

import com.sleekydz86.lazylab.modelconfig.domain.ConfigAccessResult;
import com.sleekydz86.lazylab.modelconfig.domain.ConfigStatus;
import com.sleekydz86.lazylab.modelconfig.domain.ModelConfig;

public final class ModelConfigWebModels {

	private ModelConfigWebModels() {
	}

	public record StatusResponse(String status, String statusLabel) {

		public static StatusResponse from(ConfigStatus configStatus) {
			return new StatusResponse(configStatus.status().name(), configStatus.status().label());
		}
	}

	public record ModelConfigView(
		String modelName,
		String provider,
		double temperature,
		int maxTokens,
		String endpoint
	) {

		static ModelConfigView from(ModelConfig config) {
			return new ModelConfigView(
				config.modelName(),
				config.provider(),
				config.temperature(),
				config.maxTokens(),
				config.endpoint()
			);
		}
	}

	public record AccessResponse(
		String status,
		String statusLabel,
		boolean firstAccess,
		long loadTimeMillis,
		ModelConfigView config
	) {

		public static AccessResponse from(ConfigAccessResult result) {
			return new AccessResponse(
				result.status().name(),
				result.status().label(),
				result.firstAccess(),
				result.loadTimeMillis(),
				ModelConfigView.from(result.config())
			);
		}
	}
}
