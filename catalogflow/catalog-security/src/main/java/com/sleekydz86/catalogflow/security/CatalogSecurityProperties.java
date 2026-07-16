package com.sleekydz86.catalogflow.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public class CatalogSecurityProperties {

	private boolean enabled = true;
	private String audience = "catalogflow-api";
	private String issuerUri = "http://localhost:8080/realms/catalogflow";
	private String jwtDecoderMode = "issuer";
	private String symmetricSecret = "catalogflow-test-symmetric-secret-key-32bytes";

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getAudience() {
		return audience;
	}

	public void setAudience(String audience) {
		this.audience = audience;
	}

	public String getIssuerUri() {
		return issuerUri;
	}

	public void setIssuerUri(String issuerUri) {
		this.issuerUri = issuerUri;
	}

	public String getJwtDecoderMode() {
		return jwtDecoderMode;
	}

	public void setJwtDecoderMode(String jwtDecoderMode) {
		this.jwtDecoderMode = jwtDecoderMode;
	}

	public String getSymmetricSecret() {
		return symmetricSecret;
	}

	public void setSymmetricSecret(String symmetricSecret) {
		this.symmetricSecret = symmetricSecret;
	}
}
