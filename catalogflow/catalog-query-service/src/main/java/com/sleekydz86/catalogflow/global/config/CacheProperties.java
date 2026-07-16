package com.sleekydz86.catalogflow.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cache")
public class CacheProperties {

	private boolean enabled = true;
	private long productTtlSeconds = 300;
	private long categoryTtlSeconds = 120;
	private long popularTtlSeconds = 60;
	private long nullTtlSeconds = 60;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public long getProductTtlSeconds() {
		return productTtlSeconds;
	}

	public void setProductTtlSeconds(long productTtlSeconds) {
		this.productTtlSeconds = productTtlSeconds;
	}

	public long getCategoryTtlSeconds() {
		return categoryTtlSeconds;
	}

	public void setCategoryTtlSeconds(long categoryTtlSeconds) {
		this.categoryTtlSeconds = categoryTtlSeconds;
	}

	public long getPopularTtlSeconds() {
		return popularTtlSeconds;
	}

	public void setPopularTtlSeconds(long popularTtlSeconds) {
		this.popularTtlSeconds = popularTtlSeconds;
	}

	public long getNullTtlSeconds() {
		return nullTtlSeconds;
	}

	public void setNullTtlSeconds(long nullTtlSeconds) {
		this.nullTtlSeconds = nullTtlSeconds;
	}
}
