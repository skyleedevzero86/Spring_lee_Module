package com.sleekydz86.catalogflow.global.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

	private String provider = "s3";
	private String bucket;
	private String region = "ap-northeast-2";
	private String endpoint;
	private String accessKey;
	private String secretKey;
	private boolean pathStyleAccess = true;
	private long maxFileSizeBytes = 10_485_760L;
	private long presignDurationSeconds = 900;
	private List<String> allowedContentTypes = new ArrayList<>(List.of(
			"image/jpeg",
			"image/png",
			"image/webp",
			"image/gif"));

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public boolean isPathStyleAccess() {
		return pathStyleAccess;
	}

	public void setPathStyleAccess(boolean pathStyleAccess) {
		this.pathStyleAccess = pathStyleAccess;
	}

	public long getMaxFileSizeBytes() {
		return maxFileSizeBytes;
	}

	public void setMaxFileSizeBytes(long maxFileSizeBytes) {
		this.maxFileSizeBytes = maxFileSizeBytes;
	}

	public long getPresignDurationSeconds() {
		return presignDurationSeconds;
	}

	public void setPresignDurationSeconds(long presignDurationSeconds) {
		this.presignDurationSeconds = presignDurationSeconds;
	}

	public List<String> getAllowedContentTypes() {
		return allowedContentTypes;
	}

	public void setAllowedContentTypes(List<String> allowedContentTypes) {
		this.allowedContentTypes = allowedContentTypes;
	}
}
