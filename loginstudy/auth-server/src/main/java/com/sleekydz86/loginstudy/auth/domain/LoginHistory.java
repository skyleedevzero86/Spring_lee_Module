package com.sleekydz86.loginstudy.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "login_history")
public class LoginHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String username;

	@Column(nullable = false)
	private boolean success;

	@Column(name = "ip_address", length = 64)
	private String ipAddress;

	@Column(name = "user_agent", length = 512)
	private String userAgent;

	@Column(name = "failure_reason", length = 255)
	private String failureReason;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt = Instant.now();

	protected LoginHistory() {
	}

	public LoginHistory(String username, boolean success, String ipAddress, String userAgent, String failureReason) {
		this.username = username;
		this.success = success;
		this.ipAddress = ipAddress;
		this.userAgent = userAgent;
		this.failureReason = failureReason;
	}

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
