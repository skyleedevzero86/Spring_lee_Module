package com.sleekydz86.catalogflow.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.network")
public class CatalogNetworkProperties {

	private boolean publicAccessEnabled = false;
	private int mainServicePort = 8081;
	private String mainServiceName = "catalog-command-service";
	private boolean actuatorAccessEnabled = true;

	public boolean isPublicAccessEnabled() {
		return publicAccessEnabled;
	}

	public void setPublicAccessEnabled(boolean publicAccessEnabled) {
		this.publicAccessEnabled = publicAccessEnabled;
	}

	public int getMainServicePort() {
		return mainServicePort;
	}

	public void setMainServicePort(int mainServicePort) {
		this.mainServicePort = mainServicePort;
	}

	public String getMainServiceName() {
		return mainServiceName;
	}

	public void setMainServiceName(String mainServiceName) {
		this.mainServiceName = mainServiceName;
	}

	public boolean isActuatorAccessEnabled() {
		return actuatorAccessEnabled;
	}

	public void setActuatorAccessEnabled(boolean actuatorAccessEnabled) {
		this.actuatorAccessEnabled = actuatorAccessEnabled;
	}
}
